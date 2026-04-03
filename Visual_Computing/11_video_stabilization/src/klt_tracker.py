import numpy as np
import argparse
from typing import Optional, Union

from src.detectors import FeatureDetector
from src.stabilizer import Stabilizer
import cv2

class KLTTracker:
    """
    A robust Kanade-Lucas-Tomasi (KLT) feature tracker.
    This version can be composed with a Stabilizer object for video stabilization.
    """

    # --- Configuration ---
    LK_PARAMS = dict(
        winSize=(15, 15),
        maxLevel=2,
        criteria=(cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 10, 0.03)
    )
    REDETECTION_THRESHOLD_RATIO = 0.5
    TRACK_COLOR_COUNT = 100

    def __init__(self, feature_detector: FeatureDetector, 
                 lk_params: Optional[dict] = None,
                 stabilizer: Optional[Stabilizer] = None):
        """
        Initializes the tracker with a feature detector instance and optional LK, 
        downscaling, and stabilization parameters. The tracker no longer manages the video source directly.
        """
        self.feature_detector = feature_detector
        self.lk_params = lk_params if lk_params is not None else self.LK_PARAMS
        self.stabilizer = stabilizer
        self.visualization_settings = {
            'show_win_size': False,
            'show_feature_count': True,
            'show_motion_paths': False,
        }

        self.prev_gray: Optional[np.ndarray] = None
        self.p0: Optional[np.ndarray] = None
        self.mask: Optional[np.ndarray] = None
        self.colors = np.random.randint(0, 255, (self.TRACK_COLOR_COUNT, 3))
        self.redetection_threshold = 0
        
        self.raw_path_history = []
        self.smoothed_path_history = []
        self.path_draw_origin = None
        self.MAX_PATH_LENGTH = 100

    def _detect_features(self, frame: np.ndarray) -> Optional[np.ndarray]:
        return self.feature_detector.detect(frame)

    def initialize(self, initial_frame: np.ndarray):
        """
        Initializes the tracker with the first frame.
        This must be called before _process_frame.
        """
        frame = initial_frame
        self.prev_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        self.p0 = self._detect_features(self.prev_gray)
        self.mask = np.zeros_like(frame)
        self.redetection_threshold = int(self.feature_detector.get_max_features() * self.REDETECTION_THRESHOLD_RATIO)

    def _track_features(self, frame_gray):
        """Calculates optical flow to track features."""
        if self.p0 is None:
            return None, None
        p1, st, err = cv2.calcOpticalFlowPyrLK(self.prev_gray, frame_gray, self.p0, None, **self.lk_params)
        if p1 is None or st is None:
            return None, None
        
        good_new = p1[st == 1]
        good_old = self.p0[st == 1]
        return good_old, good_new

    def _stabilize_frame(self, frame, good_old, good_new):
        """Applies stabilization and returns the transformation matrix and warped points."""
        M, display_points = None, good_new
        if self.stabilizer and len(good_old) > 0 and len(good_new) > 0:
            M, _, _ = self.stabilizer.stabilize(good_old, good_new)
            if M is not None:
                display_points = cv2.transform(good_new.reshape(-1, 1, 2), M).reshape(-1, 2)
        return M, display_points

    def _draw_visualizations(self, output_frame, M, display_points, good_old, good_new):
        """Draws all visual elements onto the frame and mask."""
        # Draw feature trails on the persistent, un-warped mask
        for i, (new, old) in enumerate(zip(good_new, good_old)):
            a, b = new.ravel()
            c, d = old.ravel()
            color = self.colors[i % self.TRACK_COLOR_COUNT].tolist()
            cv2.line(self.mask, (int(a), int(b)), (int(c), int(d)), color, 2)

        # Draw feature points (circles) on the (potentially warped) output frame
        for i, disp_pt in enumerate(display_points):
            a, b = disp_pt.ravel()
            color = self.colors[i % self.TRACK_COLOR_COUNT].tolist()
            cv2.circle(output_frame, (int(a), int(b)), 5, color, -1)
            
            if self.visualization_settings.get('show_win_size', False):
                win_w, win_h = self.lk_params['winSize']
                cv2.rectangle(output_frame, 
                              (int(a - win_w/2), int(b - win_h/2)), 
                              (int(a + win_w/2), int(b + win_h/2)), 
                              (0, 255, 255), 1)
        
        # Now, warp the trail mask to match the output frame
        final_mask = self.mask
        if M is not None:
            h, w = output_frame.shape[:2]
            final_mask = cv2.warpAffine(self.mask, M, (w, h))
            final_mask = self.stabilizer.fix_border(final_mask)

        # Combine frame with visualizations
        output_frame = cv2.add(output_frame, final_mask)

        # Feature Count overlay
        if self.visualization_settings.get('show_feature_count', False):
            max_feat = self.feature_detector.get_max_features()
            count_text = f"Features: {len(self.p0) if self.p0 is not None else 0} / {max_feat}"
            cv2.putText(output_frame, count_text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
            
        return output_frame

    def _process_frame(self, frame: np.ndarray) -> np.ndarray:
        frame_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        output_frame = frame.copy()
        
        # Apply fade effect to the trail mask
        if self.mask is not None:
            self.mask = cv2.convertScaleAbs(self.mask, alpha=0.95)

        # 1. Handle Feature Redetection
        self.redetection_threshold = int(self.feature_detector.get_max_features() * self.REDETECTION_THRESHOLD_RATIO)
        if self.p0 is None or len(self.p0) < self.redetection_threshold:
            self.p0 = self._detect_features(self.prev_gray)
            if self.mask is not None:
                self.mask.fill(0)

        if self.p0 is None:
            self.prev_gray = frame_gray
            return frame

        # 2. Track Features
        good_old, good_new = self._track_features(frame_gray)
        
        if good_old is None or good_new is None:
            self.p0 = None
            self.prev_gray = frame_gray
            return output_frame

        # 3. Stabilize Frame
        M, display_points = self._stabilize_frame(frame, good_old, good_new)
        if M is not None:
            h, w = frame.shape[:2]
            output_frame = cv2.warpAffine(output_frame, M, (w, h))
            output_frame = self.stabilizer.fix_border(output_frame)

        # 4. Draw Visualizations
        output_frame = self._draw_visualizations(output_frame, M, display_points, good_old, good_new)

        # 5. Update State for Next Frame
        self.p0 = good_new.reshape(-1, 1, 2)
        self.prev_gray = frame_gray
        
        return output_frame

import cv2
import numpy as np
from PySide6.QtCore import QThread, Signal
import time
from threading import Lock

from src.klt_tracker import KLTTracker
from src.stabilizer import Stabilizer

class VideoProcessor:
    """
    A class that handles all video capture and processing logic,
    decoupled from the Qt GUI framework.
    """
    def __init__(self, frame_callback):
        self.frame_callback = frame_callback
        self.is_running = True
        self.is_paused = False
        self.video_source = None
        self.loop_video = True
        self.playback_speed = 1.0
        self.fps = 30
        self.stabilization_enabled = False
        self.feature_detector = None
        self.stabilizer = Stabilizer()
        self.tracker: KLTTracker = None
        self.lk_params = KLTTracker.LK_PARAMS.copy()
        self.visualization_settings = {}
        self._source_changed = False
        self.reinitialize_tracker = False
        self.reset_requested = False
        self.restart_requested = False
        self.mutex = Lock()

    def _get_state_for_frame(self):
        """Safely get a snapshot of the current state from within the lock."""
        source_changed = self._source_changed
        reinit_tracker = self.reinitialize_tracker or (self.tracker is None and self.video_source is not None)
        restart_req = self.restart_requested

        state = {
            "current_source": self.video_source,
            "current_detector": self.feature_detector,
            "lk_params": self.lk_params,
            "stabilization_enabled": self.stabilization_enabled,
            "is_paused": self.is_paused,
            "speed": self.playback_speed,
            "fps": self.fps,
            "loop": self.loop_video
        }

        self._source_changed = False
        self.reinitialize_tracker = False
        self.restart_requested = False
        
        return source_changed, reinit_tracker, restart_req, state

    def _handle_source_change(self, cap, old_source, new_source):
        """Handles opening and closing the video capture source."""
        if cap:
            cap.release()
        
        if new_source is None:
            return None

        try:
            cap = cv2.VideoCapture(new_source)
            if not cap.isOpened():
                print(f"Error: Could not open video source: {new_source}")
                with self.mutex: self.video_source = None
                return None
            else:
                self.stabilizer.reset()
                return cap
        except Exception as e:
            print(f"Error opening video source: {e}")
            with self.mutex: self.video_source = None
            return None

    def _reinitialize_tracker(self, cap, state):
        """Initializes a new KLTTracker instance."""
        try:
            stabilizer_instance = self.stabilizer if state["stabilization_enabled"] else None
            new_tracker = KLTTracker(
                feature_detector=state["current_detector"],
                lk_params=state["lk_params"],
                stabilizer=stabilizer_instance
            )
            
            ret, first_frame = cap.read()
            if ret:
                if isinstance(state["current_source"], int):
                    first_frame = cv2.flip(first_frame, 1)
                
                new_tracker.initialize(first_frame)
                
                with self.mutex:
                    self.tracker = new_tracker
                    new_fps = cap.get(cv2.CAP_PROP_FPS)
                    self.fps = new_fps if new_fps > 0 else 30
                return True
            else:
                return False
        except Exception as e:
            print(f"Error initializing tracker: {e}")
            with self.mutex: self.tracker = None
            return False
        
    def _update_tracker_params(self):
        with self.mutex:
            self.tracker.lk_params = self.lk_params
            self.tracker.visualization_settings = self.visualization_settings
            if self.reset_requested:
                self.tracker.p0 = None
                if self.tracker.mask is not None: self.tracker.mask.fill(0)
                self.reset_requested = False

    def _handle_video_end(self, cap, state):
        if isinstance(state["current_source"], str) and state["loop"]:
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
            with self.mutex: self.reset_requested = True
            return True, cap # Looping, cap is still valid
        else:
            if cap: cap.release()
            with self.mutex:
                if isinstance(state["current_source"], str): self.video_source = None
            return False, None # End of stream, cap is released


    
    def _process_frame_and_handle_loop(self, cap, state):
        """Reads a frame, processes it, and handles end-of-video logic."""
        ret, frame = cap.read()
        if ret:
            if isinstance(state["current_source"], int):
                frame = cv2.flip(frame, 1)
            
            # Update tracker parameters under lock before processing
            self._update_tracker_params()

            processed_frame = self.tracker._process_frame(frame)
            self.frame_callback(processed_frame)
            
            if isinstance(state["current_source"], str) and state["speed"] > 0 and state["fps"] > 0:
                time.sleep(1 / (state["fps"] * state["speed"]))
            return True, cap # Frame processed, cap is still valid
        
        
        still_processing, curr_cap = self._handle_video_end(cap,state)
        return still_processing, curr_cap
        
    def run(self):
        cap = None
        last_source = None
        
        while self.is_running:
            with self.mutex:
                if not self.is_running: break
                source_changed, reinit_tracker, restart_req, state = self._get_state_for_frame()

            # --- Heavy work happens outside the lock ---
            if source_changed:
                cap = self._handle_source_change(cap, last_source, state["current_source"])
                last_source = state["current_source"]
                reinit_tracker = True

            if restart_req and cap and isinstance(state["current_source"], str):
                cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                with self.mutex: self.reset_requested = True

            if reinit_tracker and cap and state["current_detector"]:
                if not self._reinitialize_tracker(cap, state):
                    if cap: cap.release(); cap = None
                    last_source = None
                    with self.mutex: self.video_source = None

            if state["is_paused"] or not self.tracker or not cap or not cap.isOpened():
                time.sleep(0.02)
                continue

            # --- Frame Processing ---
            still_processing, cap = self._process_frame_and_handle_loop(cap, state)
            if not still_processing:
                last_source = None
        
        if cap:
            cap.release()

    def stop(self):
        with self.mutex:
            self.is_running = False

    def change_source(self, s):
        with self.mutex:
            if self.video_source == s: return
            self.video_source = s
            self.is_paused = False
            self._source_changed = True

    def set_feature_detector(self, d): 
        with self.mutex:
            self.feature_detector = d
            self.reinitialize_tracker = True

    def set_stabilization(self, enabled: bool):
        with self.mutex:
            self.stabilization_enabled = enabled
            if enabled:
                self.stabilizer.reset()
            self.reinitialize_tracker = True

    def update_lk_params(self, p):
        with self.mutex: self.lk_params = p
    
    def update_visualization(self, v):
        with self.mutex: self.visualization_settings = v

    def request_detector_reset(self):
        with self.mutex: self.reset_requested = True

    def pause(self):
        with self.mutex: self.is_paused = True
    
    def play(self):
        with self.mutex: self.is_paused = False

    def restart_video(self):
        with self.mutex: self.restart_requested = True
        
    def update_playback_speed(self, s):
        with self.mutex: self.playback_speed = max(0.1, s)
    
    def set_loop_video(self, l: bool):
        with self.mutex: self.loop_video = l



class VideoThread(QThread):
    """
    A QThread that wraps the VideoProcessor, acting as a bridge between
    the core processing logic and the Qt GUI.
    """
    frame_ready = Signal(np.ndarray)

    def __init__(self):
        super().__init__()
        # The VideoProcessor does all the work. The callback connects its
        # output back to this thread's signal.
        self.processor = VideoProcessor(self.frame_ready.emit)

    def run(self):
        self.processor.run()

    def stop(self): self.processor.stop()
    def change_source(self, s): self.processor.change_source(s)
    def set_feature_detector(self, d): self.processor.set_feature_detector(d)
    def set_stabilization(self, enabled: bool): self.processor.set_stabilization(enabled)
    def update_lk_params(self, p): self.processor.update_lk_params(p)
    def update_visualization(self, v): self.processor.update_visualization(v)
    def request_detector_reset(self): self.processor.request_detector_reset()
    def pause(self): self.processor.pause()
    def play(self): self.processor.play()
    def restart_video(self): self.processor.restart_video()
    def update_playback_speed(self, s): self.processor.update_playback_speed(s)
    def set_loop_video(self, l: bool): self.processor.set_loop_video(l)
    def update_downscale_factor(self, f: float): self.processor.update_downscale_factor(f)

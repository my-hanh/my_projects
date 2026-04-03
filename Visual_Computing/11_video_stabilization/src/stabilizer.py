import numpy as np
import cv2

class Stabilizer:
    def __init__(self, smoothing_radius=30):
        self.smoothing_radius = smoothing_radius
        self.reset()

    def reset(self):
        self.trajectory = [] # List of (dx, dy, da)
        self.smooth_trajectory = [] # List of smoothed (dx, dy, da)
        # Cumulative transforms (x, y, angle)
        self.cumulative_x = 0
        self.cumulative_y = 0
        self.cumulative_a = 0

    #def stabilize(self, prev_pts, curr_pts):
        """
        TODO: IMPLEMENT STABILIZATION LOGIC

        This is the core of your task for Part 2. You need to implement the
        steps to calculate the corrective transformation matrix `m`.

        The process is:
        1.  Estimate the frame-to-frame motion. A good function for this is
            cv2.estimateAffinePartial2D(), which gives you a robust estimate
            of translation and rotation. It takes the previous points and
            current points as input.

        2.  Extract the translation (dx, dy) and rotation (da) from the
            estimated transformation matrix. If the estimation fails, it will
            return None, so you should handle that case.

        3.  Accumulate these values over time in `self.cumulative_x`,
            `self.cumulative_y`, and `self.cumulative_a`. Store each new
            cumulative point in `self.trajectory`.

        4.  Smooth the raw trajectory. Take a window of the most recent
            trajectory points (of size `self.smoothing_radius`) and compute
            the average to get `smooth_x`, `smooth_y`, and `smooth_a`.
            `np.mean(window, axis=0)` is great for this.

        5.  Calculate the difference between the smoothed trajectory and the
            raw (cumulative) trajectory.

        6.  Calculate the new, corrected motion by adding this difference to
            the original frame-to-frame motion.

        7.  Construct a new 2x3 transformation matrix `m` from the corrected
            motion. This matrix will be used to warp the video frame.
        """

        # NOTE: This function should return the corrective matrix `m`,
        # the raw frame-to-frame motion delta, and the smoothed delta.
        # For now, it returns dummy values.
        """
        m = np.array(
            [[1,0,0],[0,1,0]]
        ,dtype=np.float32).reshape((2,3))
        raw_delta = (0, 0)
        smooth_delta = (0, 0)
        return m, raw_delta, smooth_delta
        """

    def stabilize(self, prev_pts, curr_pts):
        if prev_pts is None or curr_pts is None or len(prev_pts) < 3:
            M = np.float32([[1, 0, 0],
                            [0, 1, 0]])
            raw_delta = (0, 0)
            smooth_delta = (0, 0)
            return M, raw_delta, smooth_delta

        prev_pts = prev_pts.reshape(-1, 1, 2).astype(np.float32)
        curr_pts = curr_pts.reshape(-1, 1, 2).astype(np.float32)

        T, inliers = cv2.estimateAffinePartial2D(
            prev_pts, curr_pts,
            method=cv2.RANSAC,
            ransacReprojThreshold=3.0
        )

        if T is None:
            T = np.float32([[1, 0, 0],
                            [0, 1, 0]])

        dx = T[0, 2]
        dy = T[1, 2]
        da = np.arctan2(T[1, 0], T[0, 0])

        raw_delta = (dx, dy)

        self.cumulative_x += dx
        self.cumulative_y += dy
        self.cumulative_a += da

        curr_traj = np.array([self.cumulative_x,
                              self.cumulative_y,
                              self.cumulative_a])
        self.trajectory.append(curr_traj)

        radius = self.smoothing_radius
        idx = len(self.trajectory) - 1

        start = max(0, idx - radius)
        end = min(len(self.trajectory) - 1, idx + radius)

        window = np.array(self.trajectory[start:end + 1])
        smooth_x, smooth_y, smooth_a = np.mean(window, axis=0)

        diff_x = smooth_x - self.cumulative_x
        diff_y = smooth_y - self.cumulative_y
        diff_a = smooth_a - self.cumulative_a

        smooth_delta = (smooth_x, smooth_y)

        corrected_x = dx + diff_x
        corrected_y = dy + diff_y
        corrected_a = da + diff_a

        cos_a = np.cos(corrected_a)
        sin_a = np.sin(corrected_a)

        M = np.float32([
            [cos_a, -sin_a, corrected_x],
            [sin_a, cos_a, corrected_y]
        ])

        return M, raw_delta, smooth_delta

    def fix_border(self, frame):
        """
         scales the image slightly (1.04x) to hide black borders created by stabilization.
         Reference: DataHacker Tutorial "fixBorder"
        """
        s = frame.shape
        # Scale image 4% to hide borders
        T = cv2.getRotationMatrix2D((s[1]/2, s[0]/2), 0, 1.04)
        frame = cv2.warpAffine(frame, T, (s[1], s[0]))
        return frame
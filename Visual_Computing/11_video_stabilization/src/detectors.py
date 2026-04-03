import cv2
import numpy as np
from abc import ABC, abstractmethod

class FeatureDetector(ABC):
    """
    An abstract base class for feature detectors.
    It defines a common interface for detecting features in an image and
    managing parameters, allowing different algorithms to be used interchangeably.
    """
    @abstractmethod
    def detect(self, image: np.ndarray) -> np.ndarray:
        """
        Detects features in a given grayscale image.

        Args:
            image: The grayscale input image.

        Returns:
            An array of detected points in the format required by cv2.calcOpticalFlowPyrLK,
            which is [[[x1, y1]], [[x2, y2]], ...].
        """
        pass

    @staticmethod
    @abstractmethod
    def get_name() -> str:
        """Returns the display name of the detector."""
        pass

    @abstractmethod
    def get_params(self) -> dict:
        """
        Returns a dictionary defining the detector's tunable parameters,
        their ranges, and default values. This is used to build the GUI.
        
        Example format:
        {
            'param_name': {'min': 0, 'max': 100, 'value': 50, 'type': 'slider', 
                           'formatter': lambda x: f"{x/10.0:.1f}",
                           'tooltip': 'An example parameter.'}
        }
        """
        pass

    @abstractmethod
    def set_params(self, params: dict):
        """
        Sets the detector's internal parameters.

        Args:
            params: A dictionary with parameter names as keys and their new values.
        """
        pass

    @abstractmethod
    def get_max_features(self) -> int:
        """Returns the configured maximum number of features for the detector."""
        pass


class HarrisDetector(FeatureDetector):
    """
    A feature detector using the Harris Corner Detection algorithm,
    implemented via cv2.goodFeaturesToTrack with useHarrisDetector=True.
    Includes optional Gaussian blur preprocessing.
    """
    def __init__(self):
        self.params = {
            'maxCorners': 100,
            'qualityLevel': 0.01, # Corresponds to min_eigen_value / max_eigen_value
            'minDistance': 10,
            'blockSize': 3, # Size of an average block for computing a derivative covariation matrix over a pixel neighborhood
            'apply_gaussian_blur': True,
            'gaussian_ksize': 5, # Must be odd
        }

    @staticmethod
    def get_name() -> str:
        return "Harris (Good Features)"

    def detect(self, image: np.ndarray) -> np.ndarray:
        processed_image = image
        if self.params['apply_gaussian_blur']:
            ksize = self.params['gaussian_ksize']
            processed_image = cv2.GaussianBlur(image, (ksize, ksize), 0)

        # Use goodFeaturesToTrack with Harris detector
        points = cv2.goodFeaturesToTrack(
            processed_image,
            maxCorners=self.params['maxCorners'],
            qualityLevel=self.params['qualityLevel'],
            minDistance=self.params['minDistance'],
            blockSize=self.params['blockSize'],
            useHarrisDetector=True,
        )
        return points

    def get_max_features(self) -> int:
        return self.params['maxCorners']

    def get_params(self) -> dict:
        return {
            'maxCorners': {'min': 1, 'max': 5000, 'value': self.params['maxCorners'], 'type': 'slider', 'tooltip': 'Maximum number of corners to detect.'},
            'qualityLevel': {'min': 1, 'max': 1000, 'value': int(self.params['qualityLevel'] * 1000), 'type': 'slider', 'formatter': lambda x: f"{x/1000.0:.3f}", 'tooltip': 'Minimum quality of a corner (0.001 to 1.0).'},
            'minDistance': {'min': 1, 'max': 100, 'value': self.params['minDistance'], 'type': 'slider', 'tooltip': 'Minimum Euclidean distance between detected corners.'},
            'blockSize': {'min': 3, 'max': 31, 'value': self.params['blockSize'], 'type': 'slider', 'step': 2, 'formatter': lambda x: str(x | 1), 'tooltip': 'Size of the pixel neighborhood for corner detection.'},
            'apply_gaussian_blur': {'value': self.params['apply_gaussian_blur'], 'type': 'checkbox', 'tooltip': 'Apply Gaussian blur before corner detection to reduce noise.'},
            'gaussian_ksize': {'min': 3, 'max': 25, 'value': self.params['gaussian_ksize'], 'type': 'slider', 'step': 2, 'formatter': lambda x: str(x | 1), 'tooltip': 'Gaussian kernel size (must be odd).'},
        }

    def set_params(self, params: dict):
        if 'maxCorners' in params: self.params['maxCorners'] = params['maxCorners']
        if 'qualityLevel' in params: self.params['qualityLevel'] = params['qualityLevel'] / 1000.0
        if 'minDistance' in params: self.params['minDistance'] = params['minDistance']
        if 'blockSize' in params: self.params['blockSize'] = params['blockSize'] | 1
        if 'apply_gaussian_blur' in params: self.params['apply_gaussian_blur'] = params['apply_gaussian_blur']
        if 'gaussian_ksize' in params: self.params['gaussian_ksize'] = params['gaussian_ksize'] | 1
import sys
import cv2
import numpy as np
from PySide6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QLabel,  QHBoxLayout, QFileDialog,
    QSizePolicy, QScrollArea
)
from PySide6.QtCore import Qt, QTimer
from PySide6.QtGui import QImage, QPixmap
from src.detectors import HarrisDetector
from src.ui_components import  ControlPanel
from src.dark_theme import STYLE_SHEET

class MainWindow(QMainWindow):
    """Main application window with redesigned UI."""
    def __init__(self):
        super().__init__()
        self.setWindowTitle("KLT Feature Tracker")
        self.setStyleSheet(STYLE_SHEET)

        screen = QApplication.primaryScreen()
        available_geometry = screen.availableGeometry()
        self.setGeometry(100, 100, min(1600, available_geometry.width()), min(900, available_geometry.height()))

        self.detector_map = {d.get_name(): d() for d in [HarrisDetector]}
        self.current_detector_inst = None

        self.central_widget = QWidget()
        self.main_layout = QHBoxLayout(self.central_widget)
        self.setCentralWidget(self.central_widget)

        self.video_label = QLabel("Select an Input Source to begin.")
        self.video_label.setObjectName("VideoLabel")
        self.video_label.setSizePolicy(QSizePolicy.Policy.Ignored, QSizePolicy.Policy.Ignored)
        self.video_label.setScaledContents(True)
        self.video_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.main_layout.addWidget(self.video_label, 4)

        self.controls = ControlPanel(self.detector_map)
        scroll_area = QScrollArea()
        scroll_area.setWidgetResizable(True)
        scroll_area.setWidget(self.controls)
        scroll_area.setMinimumWidth(380)
        scroll_area.setMaximumWidth(450)
        self.main_layout.addWidget(scroll_area, 1)

        self.video_file_path = None
        self.video_thread = VideoThread()
        self.video_thread.frame_ready.connect(self.update_frame)
        self.video_thread.start()

        self._connect_signals()
        
        # Automatically select and initialize the only available detector
        if self.detector_map:
            first_detector_name = next(iter(self.detector_map))
            self.on_detector_changed(first_detector_name)
        
        QTimer.singleShot(0, lambda: self.on_source_changed(0))

    def _connect_signals(self):
        self.controls.detector_params_changed.connect(self.on_detector_params_changed)
        self.controls.lk_params_changed.connect(self.on_lk_params_changed)
        self.controls.source_combo.currentIndexChanged.connect(self.on_source_changed)
        self.controls.open_file_button.clicked.connect(self.open_video_file)
        self.controls.play_toggled.connect(self.on_play_toggled)
        self.controls.restart_clicked.connect(self.video_thread.restart_video)
        self.controls.loop_changed.connect(self.video_thread.set_loop_video)
        self.controls.playback_speed_changed.connect(self.on_playback_speed_changed)
        self.controls.stabilization_toggled.connect(self.on_stabilization_toggled)

    def on_play_toggled(self, checked):
        if checked:
            self.video_thread.play()
            self.controls.play_pause_button.setText("Pause")
        else:
            self.video_thread.pause()
            self.controls.play_pause_button.setText("Play")

    def on_detector_changed(self, name):
        self.current_detector_inst = self.detector_map[name]
        self.controls.rebuild_detector_controls(self.current_detector_inst)
        self.video_thread.set_feature_detector(self.current_detector_inst)

    def on_detector_params_changed(self):
        self.controls.get_current_params()
        self.video_thread.request_detector_reset()

    def on_lk_params_changed(self):
        params = self.controls.get_current_params()
        if not params: return
        self.video_thread.update_lk_params(params['lk_params'])
        self.video_thread.update_visualization(params['visualization'])

    def on_playback_speed_changed(self, value):
        self.video_thread.update_playback_speed(value / 10.0)

    def on_stabilization_toggled(self, checked):
        self.video_thread.set_stabilization(checked)

    def on_source_changed(self, index):
        source_text = self.controls.source_combo.itemText(index)
        is_video_file = (source_text == "Video File")
        
        self.controls.set_playback_mode(is_video_file)

        if not is_video_file:
            self.video_label.setText("Opening webcam...")
            self.video_thread.change_source(0)
        elif self.video_file_path:
            self.video_label.setText(f"Opening file: {self.video_file_path}")
            self.video_thread.change_source(self.video_file_path)
        else:
            self.video_label.setText("No video file selected. Please open a file.")
            self.video_thread.change_source(None)

    def open_video_file(self):
        path, _ = QFileDialog.getOpenFileName(self, "Open Video File", "", "Video Files (*.mp4 *.avi *.mov)")
        if path:
            self.video_file_path = path
            self.controls.source_combo.setCurrentText("Video File")
            if self.controls.source_combo.currentIndex() == 1:
                self.on_source_changed(1)

    def update_frame(self, frame: np.ndarray):
        try:
            h, w, ch = frame.shape
            qt_image = QImage(frame.data, w, h, ch * w, QImage.Format.Format_BGR888).copy()
            pixmap = QPixmap.fromImage(qt_image)
            self.video_label.setPixmap(pixmap.scaled(self.video_label.size(), Qt.AspectRatioMode.KeepAspectRatio, Qt.TransformationMode.SmoothTransformation))
        except Exception as e:
            print(f"Error updating frame: {e}")

    def closeEvent(self, event):
        self.video_thread.stop()
        event.accept()

from src.video_processing import VideoThread

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())
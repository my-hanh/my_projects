from functools import partial
import cv2
from PySide6.QtWidgets import (
    QWidget, QLabel, QVBoxLayout, QHBoxLayout,
    QPushButton, QSlider, QFormLayout, QComboBox,
    QCheckBox
)
from PySide6.QtCore import Qt, Signal, QTimer, QPropertyAnimation, QEasingCurve

class CollapsibleBox(QWidget):
    """A custom collapsible box widget with stable animation."""
    def __init__(self, title="", parent=None):
        super().__init__(parent)
        self.setObjectName("CollapsibleBox")

        self.toggle_button = QPushButton(title)
        self.toggle_button.setObjectName("toggle_button")
        self.toggle_button.setCheckable(True)
        
        self.content_area = QWidget()
        self.content_area.setObjectName("content_area")
        self.content_area.setMaximumHeight(0)
        self.content_area.setMinimumHeight(0)
        
        self._content_layout = QVBoxLayout(self.content_area)
        self._content_layout.setContentsMargins(10, 20, 10, 20)
        self._content_layout.setSpacing(20)

        self.animation = QPropertyAnimation(self.content_area, b"maximumHeight")
        self.animation.setDuration(300)
        self.animation.setEasingCurve(QEasingCurve.Type.InOutQuart)

        main_layout = QVBoxLayout(self)
        main_layout.setSpacing(0)
        main_layout.setContentsMargins(0, 0, 0, 0)
        main_layout.addWidget(self.toggle_button)
        main_layout.addWidget(self.content_area)

        self.toggle_button.toggled.connect(self.toggle)
        self.toggle(False)

    def toggle(self, checked):
        self.toggle_button.setChecked(checked)
        content_height = self.content_area.sizeHint().height()
        self.animation.setStartValue(self.content_area.height())
        self.animation.setEndValue(content_height if checked else 0)
        arrow = "▼" if checked else "►"
        self.toggle_button.setText(f"{arrow} {self.toggle_button.text().split(' ', 1)[-1]}")
        self.animation.start()

    def add_widget(self, widget):
        self._content_layout.addWidget(widget)

    def update_content_height(self):
        """Schedules a height update to ensure layout calculations are stable."""
        QTimer.singleShot(0, self._set_final_height)

    def _set_final_height(self):
        """Sets the content area height after the event loop has processed layout changes."""
        if self.toggle_button.isChecked():
            self.animation.stop()
            final_height = self.content_area.sizeHint().height()
            self.content_area.setMaximumHeight(final_height)

class ControlPanel(QWidget):
    """The main control panel widget, redesigned with collapsible boxes."""
    detector_params_changed = Signal()
    lk_params_changed = Signal()
    detector_changed = Signal(str)
    play_toggled = Signal(bool)
    restart_clicked = Signal()
    loop_changed = Signal(bool)
    playback_speed_changed = Signal(int)
    resolution_changed = Signal(int)
    stabilization_toggled = Signal(bool)

    def __init__(self, detector_map):
        super().__init__()
        self.setObjectName("ControlPanel")
        self.detector_map = detector_map
        self.current_detector = None

        main_layout = QVBoxLayout(self)
        main_layout.setContentsMargins(10, 10, 10, 10)
        main_layout.setSpacing(15)

        self.detector_update_timer = QTimer(self)
        self.detector_update_timer.setSingleShot(True)
        self.detector_update_timer.setInterval(150)
        self.detector_update_timer.timeout.connect(self.detector_params_changed.emit)

        self.lk_update_timer = QTimer(self)
        self.lk_update_timer.setSingleShot(True)
        self.lk_update_timer.setInterval(150)
        self.lk_update_timer.timeout.connect(self.lk_params_changed.emit)

        # --- INPUT SOURCE ---
        input_box = CollapsibleBox("Input-Source")
        input_widget = QWidget()
        input_layout = QFormLayout(input_widget)
        input_layout.setSpacing(20)
        self.source_combo = QComboBox()
        self.source_combo.addItems(["Webcam", "Video File"])
        self.open_file_button = QPushButton("Open File...")
        source_layout = QHBoxLayout()
        source_layout.addWidget(self.source_combo)
        source_layout.addWidget(self.open_file_button)
        input_layout.addRow("Source", source_layout)
        self.stabilization_check = QCheckBox("Enable-Stabilizer")
        input_layout.addRow(self.stabilization_check)
        input_box.add_widget(input_widget)
        main_layout.addWidget(input_box)

        # --- PLAYBACK CONTROL ---
        self.playback_box = CollapsibleBox("Playback/Stream-Control")
        playback_widget = QWidget()
        playback_layout = QVBoxLayout(playback_widget)
        playback_layout.setSpacing(20)
        self.play_pause_button = QPushButton("Pause")
        self.play_pause_button.setCheckable(True)
        self.play_pause_button.setChecked(True)
        self.restart_button = QPushButton("Restart")
        playback_buttons = QHBoxLayout()
        playback_buttons.addWidget(self.play_pause_button)
        playback_buttons.addWidget(self.restart_button)
        playback_layout.addLayout(playback_buttons)
        self.video_controls_widget = QWidget()
        video_controls_layout = QFormLayout(self.video_controls_widget)
        video_controls_layout.setContentsMargins(0, 0, 0, 0)
        video_controls_layout.setSpacing(20)
        self.loop_video_check = QCheckBox("Loop Video")
        self.loop_video_check.setChecked(True)
        self.playback_speed_slider, speed_layout = self._create_slider(1, 40, 10, formatter=lambda x: f"{x/10.0:.1f}x")
        video_controls_layout.addRow("Playback:", self.loop_video_check)
        video_controls_layout.addRow("Speed:", speed_layout)
        playback_layout.addWidget(self.video_controls_widget)
        self.playback_box.add_widget(playback_widget)
        main_layout.addWidget(self.playback_box)

        # --- ALGORITHM SETTINGS ---
        self.algo_box = CollapsibleBox("Detector-Settings")
        algo_widget = QWidget()
        algo_layout = QVBoxLayout(algo_widget) # Changed to QVBoxLayout
        algo_layout.setSpacing(20)
        
        self.detector_params_widget = QWidget()
        self.detector_params_layout = QFormLayout(self.detector_params_widget)
        self.detector_params_layout.setContentsMargins(0, 15, 0, 0)
        self.detector_params_layout.setSpacing(20)
        algo_layout.addWidget(self.detector_params_widget)
        
        self.algo_box.add_widget(algo_widget)
        main_layout.addWidget(self.algo_box)

        # --- TRACKING PARAMETERS (LUCAS-KANADE) ---
        lk_box = CollapsibleBox("KLT-Tracking-Parameters")
        lk_widget = QWidget()
        lk_layout = QFormLayout(lk_widget)
        lk_layout.setSpacing(20)
        self.win_size, win_layout = self._create_slider(5, 31, 15, 2, formatter=lambda x: str(x | 1))
        self.max_level, level_layout = self._create_slider(0, 10, 2)
        self.lk_max_iter, iter_layout = self._create_slider(5, 50, 10)
        self.lk_epsilon, eps_layout = self._create_slider(1, 10000, 30, formatter=lambda x: f"{x/100.0:.2f}")
        lk_layout.addRow("Window Size:", win_layout)
        lk_layout.addRow("Pyramid Levels:", level_layout)
        lk_layout.addRow("Max Iterations:", iter_layout)
        lk_layout.addRow("Epsilon:", eps_layout)
        lk_box.add_widget(lk_widget)
        main_layout.addWidget(lk_box)

        # --- VIEW & DISPLAY ---
        view_box = CollapsibleBox("View & Display")
        view_widget = QWidget()
        view_layout = QFormLayout(view_widget)
        view_layout.setSpacing(20)
        self.show_win_size_check = QCheckBox("Show Search Windows")
        self.show_feature_count_check = QCheckBox("Show Feature Count")
        self.show_feature_count_check.setChecked(True)
        view_layout.addRow(self.show_win_size_check)
        view_layout.addRow(self.show_feature_count_check)
        view_box.add_widget(view_widget)
        main_layout.addWidget(view_box)

        main_layout.addStretch()

        # --- CONNECT SIGNALS ---
        self.stabilization_check.toggled.connect(self.stabilization_toggled.emit)
        self.play_pause_button.toggled.connect(self.play_toggled.emit)
        self.restart_button.clicked.connect(self.restart_clicked.emit)
        self.loop_video_check.stateChanged.connect(lambda s: self.loop_changed.emit(s == Qt.Checked))
        self.playback_speed_slider.valueChanged.connect(self.playback_speed_changed.emit)
        self.show_win_size_check.stateChanged.connect(self.lk_update_timer.start)
        self.show_feature_count_check.stateChanged.connect(self.lk_update_timer.start)
        for slider in [self.win_size, self.max_level, self.lk_max_iter, self.lk_epsilon]:
            slider.valueChanged.connect(self.lk_update_timer.start)
        
        input_box.toggle(True)
        self.algo_box.toggle(True)

    def set_playback_mode(self, is_video_file: bool):
        self.video_controls_widget.setVisible(is_video_file)
        self.restart_button.setVisible(is_video_file)
        self.play_pause_button.setChecked(True)
        self.playback_box.update_content_height()

    def rebuild_detector_controls(self, detector):
        self.current_detector = detector
        while self.detector_params_layout.count():
            item = self.detector_params_layout.takeAt(0)
            if item.widget(): item.widget().deleteLater()
            elif item.layout():
                while item.layout().count():
                    child = item.layout().takeAt(0)
                    if child.widget(): child.widget().deleteLater()

        params_def = detector.get_params()
        for name, definition in params_def.items():
            if definition['type'] == 'slider':
                widget, layout = self._create_slider(definition['min'], definition['max'], definition['value'], definition.get('step', 1), definition.get('formatter'))
                widget.valueChanged.connect(self.detector_update_timer.start)
                widget.setToolTip(definition.get('tooltip', ''))
                self.detector_params_layout.addRow(f"{name}:", layout)
            elif definition['type'] == 'checkbox':
                widget = QCheckBox(name)
                widget.setChecked(definition['value'])
                widget.stateChanged.connect(self.detector_update_timer.start)
                widget.setToolTip(definition.get('tooltip', ''))
                self.detector_params_layout.addRow(widget)
        
        self.algo_box.update_content_height()
        self.detector_params_changed.emit()

    def _create_slider(self, min_val, max_val, initial_val, step=1, formatter=None):
        slider = QSlider(Qt.Orientation.Horizontal)
        slider.setRange(min_val, max_val)
        slider.setValue(initial_val)
        slider.setSingleStep(step)
        value_label = QLabel(formatter(initial_val) if formatter else str(initial_val))
        if formatter:
            slider.valueChanged.connect(lambda v, l=value_label, f=formatter: l.setText(f(v)))
        else:
            slider.valueChanged.connect(lambda v, l=value_label: l.setText(str(v)))
        h_layout = QHBoxLayout()
        h_layout.addWidget(slider)
        h_layout.addWidget(value_label)
        return slider, h_layout

    def get_current_params(self):
        if not self.current_detector: return {}
        detector_params = {}
        params_def = self.current_detector.get_params()

        for i in range(self.detector_params_layout.rowCount()):
            label_item = self.detector_params_layout.itemAt(i, QFormLayout.ItemRole.LabelRole)
            field_item = self.detector_params_layout.itemAt(i, QFormLayout.ItemRole.FieldRole)

            if not (label_item or field_item): continue

            if not label_item and field_item and field_item.widget():
                widget = field_item.widget()
                if isinstance(widget, QCheckBox):
                    param_name = widget.text()
                    if param_name in params_def:
                        detector_params[param_name] = widget.isChecked()
            
            elif label_item and label_item.widget() and field_item:
                param_name = label_item.widget().text().replace(':', '')
                if param_name in params_def:
                    widget = None
                    if field_item.layout():
                        widget = field_item.layout().itemAt(0).widget()
                    elif field_item.widget():
                        widget = field_item.widget()
                    
                    if isinstance(widget, QSlider):
                        detector_params[param_name] = widget.value()

        if 'qualityLevel' in detector_params: detector_params['qualityLevel'] /= 1000.0 # Adjusted for Harris
        if 'scaleFactor' in detector_params: detector_params['scaleFactor'] /= 100.0
        if 'k' in detector_params: detector_params['k'] /= 1000.0
        if 'threshold' in detector_params and self.current_detector.get_name() == "Harris": detector_params['threshold'] /= 100.0
        if 'blockSize' in detector_params: detector_params['blockSize'] |= 1
        if 'ksize' in detector_params: detector_params['ksize'] |= 1
        self.current_detector.set_params(detector_params)
        return {
            'visualization': {'show_win_size': self.show_win_size_check.isChecked(), 'show_feature_count': self.show_feature_count_check.isChecked()},
            'lk_params': {'winSize': (self.win_size.value()|1, self.win_size.value()|1), 'maxLevel': self.max_level.value(), 'criteria': (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, self.lk_max_iter.value(), self.lk_epsilon.value()/100.0)}
        }

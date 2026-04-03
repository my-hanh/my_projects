# dark_theme.py

STYLE_SHEET = """
QWidget {
    background-color: #2e2e2e;
    color: #e0e0e0;
    font-family: "Segoe UI", "Helvetica Neue", "Arial", sans-serif;
    font-size: 14px;
}

QMainWindow {
    background-color: #252525;
}

/* Control Panel Styling */
#ControlPanel {
    background-color: #3c3c3c;
    border-radius: 5px;
}

/* Collapsible Box Styling */
#CollapsibleBox QPushButton#toggle_button {
    background-color: #4a4a4a;
    color: #e0e0e0;
    border: none;
    text-align: left;
    padding: 8px;
    font-weight: bold;
    border-radius: 4px;
}

#CollapsibleBox QPushButton#toggle_button:checked {
    background-color: #5a5a5a;
}

#CollapsibleBox #content_area {
    background-color: #3c3c3c;
    border: 1px solid #4a4a4a;
    border-top: none;
    border-radius: 0 0 4px 4px;
}

/* General Widget Styling */
QPushButton {
    background-color: #5a5a5a;
    border: 1px solid #6a6a6a;
    padding: 6px 12px;
    border-radius: 4px;
    min-height: 20px;
}

QPushButton:hover {
    background-color: #6a6a6a;
}

QPushButton:pressed {
    background-color: #4a4a4a;
}

QSlider::groove:horizontal {
    border: 1px solid #4a4a4a;
    height: 4px;
    background: #444;
    margin: 2px 0;
    border-radius: 2px;
}

QSlider::handle:horizontal {
    background: #8a8a8a;
    border: 1px solid #9a9a9a;
    width: 16px;
    height: 16px;
    margin: -6px 0;
    border-radius: 8px;
}

QSlider::handle:horizontal:hover {
    background: #9a9a9a;
}

QComboBox {
    background-color: #4a4a4a;
    border: 1px solid #5a5a5a;
    border-radius: 4px;
    padding: 4px;
}

QComboBox::drop-down {
    border: none;
}

QComboBox QAbstractItemView {
    background-color: #5a5a5a;
    border: 1px solid #6a6a6a;
    selection-background-color: #4a90e2;
}

QCheckBox {
    spacing: 10px;
}

QCheckBox::indicator {
    width: 16px;
    height: 16px;
}

QLabel {
    padding: 2px;
}

QFormLayout {
    horizontal-spacing: 15px;
    vertical-spacing: 10px;
}

QScrollArea {
    border: none;
}

/* Video Label Styling */
#VideoLabel {
    background-color: black;
    border: 1px solid #4a4a4a;
    border-radius: 5px;
}
"""

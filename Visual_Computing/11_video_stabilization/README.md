# KLT Feature Tracker and Video Stabilizer

This project provides a real-time implementation of the Kanade-Lucas-Tomasi (KLT) feature tracker, complete with a graphical user interface for exploring parameters and a video stabilization pipeline.

## Prerequisites

- Python 3.11+
- NumPy
- OpenCV for Python

## Installation

1. **Clone the repository or download the source code.**
2. **Check your Python version.** Before proceeding, make sure you have a compatible version of Python installed. Open your terminal and run:

   ```bash
   python --version
   # Or on some systems, you might need:
   python3 --version
   ```

   If the version is **3.11** or higher, you can continue.
3. **Create and activate a virtual environment (Recommended).** This isolates the project's dependencies from your system-wide Python packages.

   **Option A: Using `venv` (standard Python)**

   ```bash
   # Create the virtual environment in a folder named 'venv'
   python -m venv venv

   # Activate the environment (macOS/Linux)
   source venv/bin/activate

   # Activate the environment (Windows)
   # .\venv\Scripts\activate
   ```

   **Option B: Using `conda`**

   ```bash
   # Create a new conda environment named 'klt-lab' with Python 3.9
   conda create --name klt-lab python=3.11

   # Activate the environment
   conda activate klt-lab
   ```
4. **Install the required Python packages:**
   Once your virtual environment is activated, install the dependencies from the `requirements.txt` file.

   ```bash
   pip install -r requirements.txt
   ```

## Usage

This project includes a graphical user interface (GUI) for easy interaction with the KLT tracker.

### Running the GUI Application

To run the application, execute the `gui_app.py` script from your terminal:

```bash
python gui_app.py
```

@echo off
echo "Running start.bat for Quelea mDNS"
call .venv\Scripts\activate.bat
echo "Virtual Environment Loaded"
python ./main.py %*
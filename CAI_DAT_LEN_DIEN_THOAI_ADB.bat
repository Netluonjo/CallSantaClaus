@echo off
chcp 65001 >nul
echo ========================================================
echo   CÀI ĐẶT ỨNG DỤNG LÊN THIẾT BỊ ANDROID QUA ADB
echo   App: "Mô Phỏng Trò chuyện Với Santa"
echo ========================================================
set ADB_PATH=C:\Users\asus\AppData\Local\Android\Sdk\platform-tools\adb.exe

if not exist "%ADB_PATH%" (
    echo Không tìm thấy adb.exe tại %ADB_PATH%
    pause
    exit /b
)

echo Đang kiểm tra thiết bị kết nối...
"%ADB_PATH%" devices

echo.
echo Đang cài đặt file APK lên thiết bị...
"%ADB_PATH%" install -r "%~dp0app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   CÀI ĐẶT THÀNH CÔNG! Đang khởi chạy ứng dụng...
    echo ========================================================
    "%ADB_PATH%" shell am start -n com.festive.santacall/.MainActivity
) else (
    echo.
    echo Cài đặt thất bại. Hãy chắc chắn điện thoại đã bật USB Debugging hoặc Emulator đang chạy.
)

pause

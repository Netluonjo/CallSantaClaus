# 🎅 Mô Phỏng Trò chuyện Với Santa (Santa Call Simulation)

Dự án Android hoàn chỉnh mô phỏng cuộc gọi thoại, video FaceTime và nhắn tin với Ông Già Noel (Santa Claus), thiết kế chuẩn giao diện App Store với hiệu ứng mùa Giáng sinh lung linh.

---

## 🌟 Tính Năng Nổi Bật

1. **Giao Diện Banner Chuẩn App Store:**
   - Banner màu gradient Giáng sinh sang trọng đúng như ảnh mẫu.
   - Icon Ông Già Noel cầm điện thoại cổ điển, viền nút từ chối (đỏ) và video (xanh).
   - Đếm ngược thời gian thực đến Đêm Giáng Sinh (24/12).
   - Hiệu ứng tuyết rơi tự nhiên 60 FPS mượt mà.

2. **Cuộc Gọi Video Giả Lập (Fake Video Call):**
   - Video Santa Claus trò chuyện tại xưởng quà Bắc Cực.
   - Hiển thị góc Camera trước (Selfie / PiP) của bé bằng **CameraX**.
   - Hỗ trợ đổi camera trước/sau, bật/tắt micro, loa ngoài và đếm thời gian cuộc gọi.
   - Giọng nói Santa Claus phát trực tiếp chúc mừng Giáng sinh.

3. **Cuộc Gọi Thoại Giả Lập (Fake Voice Call):**
   - Màn hình cuộc gọi thoại với vòng sóng âm thanh nhấp nháy theo giọng nói Santa.
   - Bàn phím số ảo, tắt tiếng, loa ngoài và phím chuyển đổi sang Video Call.

4. **Màn Hình Cuộc Gọi Đến Siêu Thực (Incoming Call Screen):**
   - Nhạc chuông **Jingle Bells** vui tươi kết hợp rung điện thoại nhịp điệu.
   - Đầy đủ 2 nút "Nghe máy" (xanh nhấp nháy thu hút) và "Từ chối" (đỏ).
   - Có thể bật sáng màn hình kể cả khi đang khóa máy (`showWhenLocked`, `turnScreenOn`).

5. **Hẹn Giờ Đổ Chuông (Schedule Prank Call):**
   - Hẹn giờ sau 10 giây, 30 giây, 1 phút, 5 phút, 15 phút.
   - Sử dụng `AlarmManager` và `CallAlarmReceiver`, tự động đánh thức màn hình và đổ chuông ngay khi hết giờ.

6. **Chat Thông Minh Với Santa:**
   - Khung chat Giáng sinh với trạng thái "Trực tuyến tại Bắc Cực 🟢".
   - Santa trả lời thông minh, hiển thị hiệu ứng "Santa đang soạn tin nhắn...".
   - Gợi ý câu hỏi nhanh: Xin quà, hỏi thăm tuần lộc Rudolph, thời tiết Bắc Cực, kiểm tra danh sách Bé Ngoan.

7. **Cá Nhân Hóa Hồ Sơ Của Bé:**
   - Cho phép nhập tên bé (ví dụ: "Bé Bo", "Minh Anh").
   - Ghi chú món quà bé ước và lời dặn của Santa (chăm ăn rau, ngủ sớm, học giỏi).

8. **Mô Phỏng Gói Mua In-App (VIP Santa):**
   - Popup mua hàng trong ứng dụng giống hệt App Store.

---

## 🚀 Cách Chạy Dự Án

### Cách 1: Mở Bằng Android Studio
1. Nhấp đúp vào file [MO_DU_AN_TRONG_ANDROID_STUDIO.bat](file:///d:/CallSantaClaus/MO_DU_AN_TRONG_ANDROID_STUDIO.bat) hoặc mở Android Studio -> chọn `Open` -> dẫn tới thư mục `D:\CallSantaClaus`.
2. Chờ Android Studio sync Gradle (dự án đã được cấu hình thư viện offline sẵn, sync sẽ rất nhanh).
3. Bấm nút **Run (Shift + F10)** để chạy trên Emulator hoặc điện thoại thật.

### Cách 2: Cài Trực Tiếp File APK Lên Điện Thoại
* File APK đã được biên dịch sẵn tại: [SantaCall_Magic.apk](file:///d:/CallSantaClaus/SantaCall_Magic.apk)
* Hoặc cắm điện thoại Android vào máy tính (bật USB Debugging) và nhấp đúp file [CAI_DAT_LEN_DIEN_THOAI_ADB.bat](file:///d:/CallSantaClaus/CAI_DAT_LEN_DIEN_THOAI_ADB.bat) để tự động cài đặt.

---

## 🛠️ Công Nghệ Sử Dụng
* **Ngôn ngữ:** Kotlin 2.0.20
* **Giao diện:** Jetpack Compose + Material 3
* **Camera:** CameraX (Camera2, Lifecycle, PreviewView)
* **Xử lý âm thanh/video:** MediaPlayer + SoundManager custom
* **Định vị & Hẹn giờ:** AlarmManager + BroadcastReceiver + WakeLock
* **Gradle:** 8.9 (AGP 8.5.2)

# Auction-Web-GUI - Hệ thống Đấu giá trực tuyến

Hệ thống đấu giá trực tuyến được xây dựng theo mô hình Client-Server, cho phép người dùng đăng ký, đăng nhập và tham gia đấu giá các sản phẩm theo thời gian thực.

## 1\. Mô tả bài toán và Phạm vi

  * **Bài toán:** Quản lý quy trình đấu giá tài sản trực tuyến, đảm bảo tính minh bạch và cập nhật giá thầu tức thời giữa các bên.
  * **Phạm vi hệ thống:**
      * Hỗ trợ đa người dùng kết nối đồng thời qua Socket.
      * Quản lý danh mục hàng hóa (như điện tử, đồ gia dụng...).
      * Hệ thống thông báo thời gian thực khi có người trả giá cao hơn.

## 2\. Công nghệ sử dụng và Yêu cầu cài đặt

  * **Ngôn ngữ:** Java 21.
  * **Công nghệ chính:**
      * **Backend:** Java Socket, MySQL (với HikariCP để quản lý kết nối).
      * **Frontend:** JavaFX 21.
      * **Dữ liệu:** JSON (Sử dụng thư viện Gson).
      * **Lưu trữ hình ảnh:** Cloudinary.
  * **Môi trường yêu cầu:**
      * JDK 21 trở lên.
      * Apache Maven 3.x.
      * MySQL Server.

## 3\. Cấu trúc thư mục (Module chính)

Dự án được tổ chức theo mô hình Multi-module Maven:

  * `auction-parent`: Module gốc quản lý các module con.
  * `auction-common`: Chứa các lớp dùng chung như DTO, cấu hình Gson.
  * `auction-server`: Xử lý logic nghiệp vụ, kết nối cơ sở dữ liệu và quản lý Socket.
  * `auction-client`: Giao diện người dùng JavaFX và logic gửi yêu cầu đến server.

## 4\. Hướng dẫn cài đặt và Chạy chương trình

Bạn có thể chạy dự án trên Windows, Linux hoặc MacOS thông qua dòng lệnh Maven.

### Bước 1: Cài đặt thư viện

Tại thư mục gốc của dự án, chạy lệnh:

``` bash
mvn clean install

```

### Bước 2: Chạy Server

Di chuyển vào module server hoặc chạy trực tiếp từ thư mục gốc:

``` bash
mvn exec:java -pl auction-server -Dexec.mainClass="com.mikey.auction.socket.AuctionServer"

```

### Bước 3: Chạy Client

Mở một terminal mới tại thư mục gốc:

``` bash
mvn javafx:run -pl auction-client

```

*(Lưu ý: Luôn khởi động Server trước khi mở ứng dụng Client để đảm bảo kết nối)*.

## 5\. Danh sách chức năng đã hoàn thành

- [x] Hệ thống đăng ký và đăng nhập người dùng.
- [x] Chức năng đổi mật khẩu.
- [x] Tìm kiếm sản phẩm theo danh mục và ID.
- [x] Đấu giá trực tuyến (Bid) với bước giá (bid step).
- [x] Hệ thống thông báo (Notification) khi đọc tin nhắn hoặc cập nhật trạng thái đấu giá.
- [x] Quản lý kết nối cơ sở dữ liệu qua Connection Pool (HikariCP).

## 6\. Tài liệu và Demo

  * **Báo cáo chi tiết (PDF):** \[https://drive.google.com/file/d/1Hyv8QhgBYJm0vhK8OJnNiRegtWCyvAwQ/view?usp=sharing\]
  * **Video Demo sản phẩm:** \[https://youtube.com/playlist?list=PLCFv0s9sQeOUQIHXrCBaY0rw7RlzBHPdY&si=A3o2oMC_Bb9AmXEn\]

-----
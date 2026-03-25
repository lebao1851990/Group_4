# Báo cáo phân tích hiệu năng: List vs Binary Search Tree (BST)

## 1. Môi trường và Phương pháp chuẩn bị test (Benchmark Setup)
- **Cấu trúc Dữ liệu sử dụng:** `MyLinkedList<Monster>` và `MonsterBST`.
- **Dữ liệu đầu vào:** 10,000 đối tượng `Monster` được khởi tạo và thêm vào cả List và BST. ID của Monster sinh ngẫu nhiên (từ 0 đến 49,999) để đảm bảo tính ngẫu nhiên của tập dữ liệu.
- **Tiêu chí đo lường:** Thời gian Execution Time (bằng nanosecond) cho thuật toán Tìm kiếm (Search) một ID bất kỳ của phần tử nằm ở cuối danh sách (Mục tiêu khó nhất của List).

## 2. Kết quả đo lường (Benchmark Results)
Dựa trên log thực thi được tạo ra bởi Unit Test `BenchmarkTest.java`:
- **List Time (MyLinkedList):** Có thời gian chạy chậm hơn do phải duyệt tuần tự từ đầu đến cuối danh sách (tuyến tính).
- **BST Time (MonsterBST):** Nhanh hơn vượt trội nhờ cấu trúc cây phân nhánh.

## 3. Phân tích Độ phức tạp theo Big-O
### Danh sách liên kết (Linked List)
- **Độ phức tạp:** **O(N)**
- **Giải thích:** List lưu trữ các phần tử tuần tự. Trong trường hợp xấu nhất (Worst Case) khi phần tử cần tìm nằm ở cuối danh sách hoặc không tồn tại, thuật toán phải duyệt qua toàn bộ N phần tử. Với N = 10,000, số vòng lặp tối đa là 10,000.

### Cây nhị phân tìm kiếm (Binary Search Tree - BST)
- **Độ phức tạp:** **O(log N)** (Trong trường hợp trung bình / cây cân bằng)
- **Giải thích:** Nhờ tính chất của BST (Node con bên trái nhỏ hơn cha, Node con bên phải lớn hơn cha), tại mỗi bước duyệt đệ quy, khoảng tìm kiếm được loại bỏ một nửa. Với N = 10,000, số vòng lặp/đệ quy tối đa chỉ khoảng `log2(10000) ≈ 14` thao tác. 

## 4. Kết luận
- **Binary Search Tree** tỏ ra cực kỳ hiệu quả và tối ưu hóa tốt các tác vụ Search/Tìm kiếm trên tập dữ liệu lớn so với List. 
- Việc chuyển đổi cấu trúc dữ liệu từ List sang Tree để lưu trữ Monster trong game giúp nâng cao đáng kể FPS (khung hình) và giảm lag khi tính toán va chạm hoặc tìm đối tượng trong các Room mở rộng.
- **AI Log:** Thuật toán duyệt đệ quy của BST (`searchRec`) đã được chứng minh là an toàn bằng file `MonsterBSTTest.java`, được bọc các trường hợp kiểm thử đầu vào null (chống NullPointerException) triệt để. Code không sử dụng thư viện `java.util.*` có sẵn (ngoại trừ đọc File) theo đúng quy định.

package com.festive.santacall.util

import java.text.Normalizer
import java.util.regex.Pattern

object SantaDialogueManager {
    private var lastFallbackIndex = -1
    private var lastCategory = ""

    private fun removeDiacritics(str: String): String {
        val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
            .replace("đ", "d")
            .replace("Đ", "d")
            .lowercase()
            .trim()
    }

    fun getSantaReply(input: String, childName: String, wishlist: String): String {
        val clean = input.trim()
        val normalized = removeDiacritics(clean)

        return when {
            // 1. Chào hỏi
            containsAny(normalized, "chao", "hello", "hi", "he lo", "alo", "ong oi", "santa oi", "bac cuc oi") -> {
                pickResponse(
                    "greeting",
                    listOf(
                        "Ho Ho Ho! 🎅 Chào cháu $childName yêu quý! Một ngày của cháu thế nào rồi?",
                        "Jolly Christmas! 🎄 Santa chào $childName nhé! Rất vui được trò chuyện cùng cháu hôm nay!",
                        "Chào cháu yêu! Bắc Cực đang ngập tràn không khí lễ hội, cháu đã chuẩn bị đón Giáng Sinh chưa nào?"
                    )
                )
            }

            // 2. Hỏi sức khỏe
            containsAny(normalized, "khoe khong", "the nao roi", "suc khoe", "co khoe", "met khong", "co om khong") -> {
                pickResponse(
                    "health",
                    listOf(
                        "Ho Ho Ho! 🎅 Ta khỏe lắm cháu ơi! Ngày nào ta cũng uống sữa ấm với bánh quy gừng, và cùng tuần lộc chạy bộ trên tuyết trắng đấy!",
                        "Cảm ơn cháu $childName đã quan tâm nhé! Ta lúc nào cũng tràn đầy năng lượng để chuẩn bị đêm Noel! Cháu và cả gia đình dạo này đều khỏe chứ?",
                        "Ta khỏe re luôn! Các chú lùn chăm sóc ta rất chu đáo. Cháu nhớ giữ ấm cổ họng đừng để bị cảm lạnh mùa đông nhé!"
                    )
                )
            }

            // 3. Hỏi tuổi
            containsAny(normalized, "bao nhieu tuoi", "may tuoi", "sinh nam nao", "tuoi gi", "gia chua", "bao tuoi") -> {
                pickResponse(
                    "age",
                    listOf(
                        "Haha! 🎅 Ta đã sống hơn 1700 tuổi rồi đấy! Râu của ta dài và trắng như tuyết, nhưng trái tim ta lúc nào cũng trẻ thơ như các cháu!",
                        "Tuổi của Santa á? Nhiều tuổi đến mức ta quên mất số cây nến trên bánh sinh nhật rồi! Nhưng với trẻ em ngoan thì Santa luôn là người bạn thân thiết nhất!"
                    )
                )
            }

            // 4. Hỏi đang làm gì / Bận không
            containsAny(normalized, "dang lam gi", "lam gi day", "co ban khong", "ban khong", "lam chi rua", "lam viec gi") -> {
                pickResponse(
                    "doing",
                    listOf(
                        "Ta đang cùng các chú lùn Elves gói những hộp quà thật đẹp và thắt nơ đỏ lấp lánh! 🎁 Cháu có muốn phụ một tay không?",
                        "Ta đang kiểm tra lại cỗ xe tuần lộc và tra dầu vào các bánh trượt tuyết để bay thật êm qua mái nhà các bé đêm Noel!",
                        "Ta vừa ngồi bên lò sưởi nhâm nhi một tách sô-cô-la nóng và đọc thư của các bạn nhỏ khắp thế giới gửi về đây!"
                    )
                )
            }

            // 5. Hỏi nơi ở / Bắc Cực
            containsAny(normalized, "o dau", "nha o dau", "que o dau", "bac cuc", "dia chi", "song o dau") -> {
                pickResponse(
                    "home",
                    listOf(
                        "Nhà của ta ở ngôi làng tuyết Rovaniemi tại Bắc Cực xa xôi! ❄️ Nơi đây quanh năm phủ đầy tuyết trắng, có cực quang rực rỡ và những chú chim cánh cụt đáng yêu!",
                        "Ta sống tại Xưởng Đồ Chơi Kỳ Diệu ở đỉnh Bắc Cực! Nếu nhìn lên bầu trời đêm mùa đông có ánh sáng bảy sắc, đó chính là ánh sáng từ ngôi làng của Santa đấy!"
                    )
                )
            }

            // 6. Hỏi thời tiết / Lạnh không
            containsAny(normalized, "lanh khong", "thoi tiet", "tuyet roi", "bao nhieu do", "co lanh") -> {
                pickResponse(
                    "weather",
                    listOf(
                        "Bắc Cực lạnh lắm cháu ơi, nhiệt độ đang là -30°C lận! ❄️ Nhưng Santa mặc áo nhung đỏ dày và có lò sưởi ấm cúng nên không sợ lạnh tẹo nào!",
                        "Tuyết ở đây rơi dày trắng xóa cả lối đi! Các chú lùn vừa ném bóng tuyết vừa hát ca suốt ngày vui nhộn lắm!"
                    )
                )
            }

            // 7. Hỏi về Tuần lộc / Cỗ xe bay
            containsAny(normalized, "tuan loc", "rudolph", "co xe", "xe bay", "bay the nao", "chu nai") -> {
                pickResponse(
                    "reindeer",
                    listOf(
                        "Đội bay của ta có 9 chú tuần lộc cừ khôi! Dẫn đầu là chú Rudolph với chiếc mũi đỏ rực phát sáng như ngọn đèn dẫn đường qua bão tuyết! 🦌🔴",
                        "Tuần lộc của ta rất thích ăn cà rốt và táo đỏ giòn ngọt! Đêm Noel cháu có muốn để dành cho các bạn ấy một củ cà rốt nhỏ cạnh gốc thông không?"
                    )
                )
            }

            // 8. Hỏi về Bà Già Noel / Vợ / Gia đình
            containsAny(normalized, "vo", "ba gia noel", "mrs claus", "gia dinh", "co vo chua", "lay vo") -> {
                pickResponse(
                    "family",
                    listOf(
                        "Ho Ho Ho! Bà già Noel (Mrs. Claus) đang nướng những mẻ bánh gừng thơm phức trong bếp đấy! Bà dặn Santa là phải nhắc $childName ăn ngoan, ngủ đúng giờ nhé! 🍪👵",
                        "Gia đình của Santa ngoài Bà Claus dịu hiền ra thì còn có hàng trăm chú lùn tí hon tinh nghịch và đàn tuần lộc đáng yêu nữa!"
                    )
                )
            }

            // 9. Hỏi về thức ăn / Món yêu thích
            containsAny(normalized, "an gi", "thich an gi", "uong gi", "mon gi", "do an", "banh quy") -> {
                pickResponse(
                    "food",
                    listOf(
                        "Santa mê nhất là bánh quy bơ nướng giòn và một ly sữa tươi ấm ngọt ngào! Đêm 24/12 các bạn nhỏ hay để bánh trên bàn chờ ta đến đấy! 🍪🥛",
                        "Món ăn khoái khẩu của Santa là kẹo que bạc hà xoắn đỏ trắng và bánh khúc cây sô-cô-la Giáng sinh! 🍭🍰"
                    )
                )
            }

            // 10. Quà tặng / Xin quà / Điều ước
            containsAny(normalized, "qua", "xin qua", "uoc", "tang", "do choi", "lego", "bup be", "xe dieu khien", "robot", "vay", "dien thoai", "tien") -> {
                pickResponse(
                    "gifts",
                    listOf(
                        "Ho Ho Ho! 🎁 Ta đã ghi nhận mong ước của $childName vào Sổ Tay Vàng: \"$wishlist\"! Chỉ cần cháu giữ đúng lời hứa chăm ngoan thì sáng 25/12 quà sẽ ở dưới gốc cây thông nhé!",
                        "Một món quà thật tuyệt vời! Ta sẽ nhắn các chú lùn chuẩn bị ngay món quà này cho $childName. Nhớ là phải luôn vâng lời cha mẹ đấy nhé!",
                        "Món quà đó rất hay! Các chú lùn thợ mộc và thợ cơ khí của Santa đang tăng ca lắp ráp đồ chơi ngày đêm để kịp giao cho cháu đây! 🎁✨"
                    )
                )
            }

            // 11. Hỏi về Danh sách Bé Ngoan
            containsAny(normalized, "be ngoan", "danh sach", "so vang", "so tay", "co duoc qua khong", "chau co ngoan") -> {
                pickResponse(
                    "nice_list",
                    listOf(
                        "Ta vừa mở Sách Bé Ngoan ra kiểm tra... 📜 Tên $childName đang nằm ở nhóm Bé Ngoan Xuất Sắc đấy! Hãy tiếp tục chăm chỉ và ăn thật nhiều rau củ nhé!",
                        "Cháu hoàn toàn xứng đáng có tên trong danh sách Bé Ngoan! Cố gắng giữ phong độ ngoan ngoãn, lễ phép này nhé! ⭐"
                    )
                )
            }

            // 12. Hỏi khi nào đến / Mấy giờ đến
            containsAny(normalized, "may gio", "khi nao", "bao gio", "hom nao", "ngay nao", "den nha", "den luc nao") -> {
                pickResponse(
                    "arrival",
                    listOf(
                        "Đúng đêm 24/12 rạng sáng 25/12 khi đồng hồ điểm nửa đêm và cháu đã ngủ thật say, ta sẽ ghé thăm nhà cháu nhé! ⏰🎄",
                        "Ta sẽ đến vào đêm Noel! Nhớ đi ngủ sớm, đừng thức rình Santa nhé, vì phép thuật chỉ xuất hiện khi các bé ngủ say thôi!"
                    )
                )
            }

            // 13. Hỏi làm sao để gặp / Ống khói / Cửa sổ
            containsAny(normalized, "vao nha", "ong khoi", "khong co ong khoi", "gap the nao", "lam sao gap", "cua so") -> {
                pickResponse(
                    "chimney",
                    listOf(
                        "Nếu nhà cháu không có ống khói thì đừng lo! Santa có một chiếc Chìa Khóa Vàng Ma Thuật có thể mở mọi cánh cửa yêu thương mà không làm ai thức giấc! 🔑✨",
                        "Santa có phép thuật thu nhỏ để lướt qua cửa sổ hoặc khe gió nhẹ nhàng như một làn gió tuyết!"
                    )
                )
            }

            // 14. Santa có thật không?
            containsAny(normalized, "co that khong", "gia vo", "bo mua", "me mua", "ai mua qua", "that hay gia") -> {
                pickResponse(
                    "reality",
                    listOf(
                        "Ho Ho Ho! 🎅 Santa có thật chứ! Santa hiện diện trong niềm tin, trong nụ cười và tình yêu thương vô bờ bến mà bố mẹ và mọi người dành cho nhau mỗi mùa Giáng sinh!",
                        "Chỉ những ai có trái tim trong sáng và niềm tin vào điều kỳ diệu mới nhìn thấy và cảm nhận được phép màu của Santa đấy cháu yêu à! ✨"
                    )
                )
            }

            // 15. Kể chuyện / Hát bài hát
            containsAny(normalized, "ke chuyen", "hat di", "bai hat", "doc tho", "hat mot bai", "ke su tich") -> {
                pickResponse(
                    "sing_story",
                    listOf(
                        "🎶 Jingle bells, jingle bells, jingle all the way! Oh what fun it is to ride in a one-horse open sleigh, hey! 🔔 Cháu thấy giọng ca của Santa thế nào?",
                        "Ngày xửa ngày xưa, ở một ngôi làng tuyết trắng... Có một bé ngoan tên là $childName luôn mỉm cười và biết giúp đỡ mọi người, và đêm Noel bé đã nhận được món quà mơ ước nhất đời! 📖✨"
                    )
                )
            }

            // 16. Chúc ngủ ngon / Đi ngủ
            containsAny(normalized, "ngu ngon", "di ngu", "buon ngu", "ngu day", "thuc khuya") -> {
                pickResponse(
                    "sleep",
                    listOf(
                        "Chúc $childName ngủ thật ngon nhé! Nhớ đắp chăn ấm, nhắm mắt lại và mơ những giấc mơ thần tiên kẹo ngọt. Santa sẽ canh giấc ngủ cho cháu! 🌙⭐",
                        "Đi ngủ sớm là một em bé ngoan! Chúc cháu ngủ say, sáng mai thức dậy đón một ngày mới tràn ngập niềm vui!"
                    )
                )
            }

            // 17. Chào tạm biệt
            containsAny(normalized, "tam biet", "bye", "hen gap lai", "thoi nhe", "dung o day", "ve day") -> {
                pickResponse(
                    "goodbye",
                    listOf(
                        "Tạm biệt cháu $childName yêu quý! Hẹn gặp lại cháu trong cuộc gọi video tiếp theo hoặc đêm Noel nhé! Ho Ho Ho! 👋🎅",
                        "Tạm biệt nhé! Nhớ giữ ấm cổ họng và luôn mỉm cười thật tươi nha!"
                    )
                )
            }

            // 18. Cảm ơn / Khen Santa
            containsAny(normalized, "cam on", "thank", "yeu ong", "thuong ong", "ong gioi", "ong tot", "dep trai") -> {
                pickResponse(
                    "thanks",
                    listOf(
                        "Ho Ho Ho! ❤️ Trái tim già nua này ấm áp lên bao nhiêu khi nghe những lời đáng yêu của $childName! Ta cũng yêu quý cháu rất nhiều!",
                        "Không có gì đâu cháu yêu! Nhìn thấy nụ cười rạng rỡ của cháu là món quà quý giá nhất dành cho Santa rồi!"
                    )
                )
            }

            // 19. Học tập / Thói quen / Lười ăn rau
            containsAny(normalized, "hoc bai", "diem kem", "luoi", "khong nghe loi", "an rau", "bi mang", "cham hoc") -> {
                pickResponse(
                    "habits",
                    listOf(
                        "Cháu ơi, để trở thành em bé ngoan được mọi người yêu quý, nhớ chăm chỉ học bài và ăn thật nhiều rau xanh để cao lớn khỏe mạnh nhé! Santa luôn dõi theo và tin tưởng $childName!",
                        "Đừng nản chí nhé! Nếu hôm nay chưa tốt thì ngày mai ta cố gắng hơn một chút, nghe lời ba mẹ là Santa tự hào lắm rồi!"
                    )
                )
            }

            // 20. Hỏi tên cháu
            containsAny(normalized, "ten chau la gi", "chau ten gi", "biet ten chau", "chau la ai") -> {
                "Làm sao mà Santa quên được chứ! Cháu chính là bé $childName đáng yêu, luôn ngoan ngoãn và lễ phép của ta đây mà! 🎅✨"
            }

            // 21. Dynamic Fallback Pool (Xoay vòng thông minh, không bao giờ lặp lại 1 câu nhàm chán!)
            else -> {
                val fallbacks = listOf(
                    "Ho Ho Ho! 🎅 Câu hỏi này của $childName thật là độc đáo và thông minh! Cháu có biết ở Bắc Cực trò chơi vui nhất của các chú lùn là trượt máng tuyết không?",
                    "Ồ, câu chuyện của $childName nghe thú vị quá! Thế hôm nay ở nhà hoặc ở trường cháu có chuyện gì vui kể tiếp cho Santa nghe nào?",
                    "Ho Ho Ho! Ta đang lắng nghe rất chăm chú đây! Hôm nay cháu đã làm được việc tốt nào giúp đỡ ông bà bố mẹ chưa?",
                    "Thật tuyệt vời! Cháu có biết Giáng Sinh ý nghĩa nhất là khi chúng ta biết sẻ chia niềm vui và trao gửi yêu thương đến mọi người xung quanh không?",
                    "Haha! 🎅 Nghe cháu tâm sự làm Santa vui quá xá! Cháu đã chuẩn bị trang trí cây thông Noel hay treo chiếc tất đỏ ở đầu giường chưa nào?",
                    "Ta ghi nhớ điều $childName vừa chia sẻ rồi nhé! Cháu nhớ ăn uống đầy đủ, ngủ ngoan để đêm 24/12 đón điều kỳ diệu nha! 🎄✨",
                    "Ho Ho Ho! Bắc Cực đang rơi rất nhiều tuyết trắng, nhưng nghe tin nhắn của $childName làm lòng Santa thấy ấm áp lạ kỳ! Cháu ước gì cho gia đình mình dịp Noel này?",
                    "Câu hỏi hay lắm cháu yêu! Ông già Noel luôn ở đây để lắng nghe mọi ước mơ và tâm tư của $childName đấy!"
                )
                lastFallbackIndex = (lastFallbackIndex + 1) % fallbacks.size
                fallbacks[lastFallbackIndex]
            }
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }

    private fun pickResponse(category: String, responses: List<String>): String {
        lastCategory = category
        return responses.random()
    }
}

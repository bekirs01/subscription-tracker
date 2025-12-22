package com.example.subscriptiontracker.utils

import android.content.Context

object AIAssistantService {
    
    /**
     * Akıllı ve doğal AI cevapları
     * İleride gerçek AI API bağlanabilir
     */
    fun getResponse(context: Context, question: String): String {
        val lowerQuestion = question.lowercase().trim()
        
        return when {
            // Hava durumu
            containsAny(lowerQuestion, listOf("hava", "weather", "hava durumu", "hava nasıl")) -> {
                "Hava durumu bilgisi almak için şehir adını belirtin. Örneğin: 'İstanbul hava durumu' veya 'Hava nasıl?' (konum izni varsa otomatik alınır)."
            }
            
            // Günlük konuşma - Selamlaşma (Daha doğal ve sıcak)
            containsAny(lowerQuestion, listOf("naber", "n\'aber", "ne haber", "what\'s up", "whats up")) -> {
                "Naber! 😊 Subtracky'de nasıl yardımcı olabilirim? Aboneliklerinizi yönetmek, ayarları değiştirmek veya başka bir şey mi istiyorsunuz?"
            }
            
            containsAny(lowerQuestion, listOf("selam", "hey", "hi")) -> {
                "Selam! 👋 Size nasıl yardımcı olabilirim? Subtracky hakkında merak ettiğiniz bir şey var mı?"
            }
            
            containsAny(lowerQuestion, listOf("merhaba", "hello", "hi there")) -> {
                "Merhaba! 👋 Ben Subtracky'nin AI asistanıyım. Aboneliklerinizi takip etmenizde, ayarları yapılandırmanızda veya uygulamayı kullanmanızda size yardımcı olabilirim. Ne hakkında konuşmak istersiniz?"
            }
            
            containsAny(lowerQuestion, listOf("nasılsın", "how are you", "how\'s it going")) -> {
                "Teşekkürler, iyiyim! 😊 Sizin için buradayım. Subtracky ile aboneliklerinizi yönetmenize yardımcı olmak için hazırım. Size nasıl yardımcı olabilirim?"
            }
            
            // Yardım isteme (Daha samimi)
            containsAny(lowerQuestion, listOf("yardım", "help", "yardım eder misin", "can you help", "yardımcı ol")) -> {
                "Tabii ki! 😊 Hangi konuda yardıma ihtiyacınız var?\n\n" +
                "• Abonelik ekleme/düzenleme\n" +
                "• Ayarlar (dil, tema, para birimi)\n" +
                "• Bildirimler\n" +
                "• Premium özellikler\n\n" +
                "Hangisini açıklayayım?"
            }
            
            // Uygulama ne işe yarıyor (Daha açıklayıcı)
            containsAny(lowerQuestion, listOf("ne işe yarıyor", "what does it do", "bu uygulama ne", "what is this app", "nedir", "what is", "ne yapıyor")) -> {
                "Subtracky, tüm aboneliklerinizi tek yerde toplayıp takip etmenizi sağlayan modern bir uygulamadır! 📱\n\n" +
                "Yapabilecekleriniz:\n" +
                "✅ Netflix, Spotify, iCloud gibi tüm aboneliklerinizi ekleyin\n" +
                "✅ Yenileme tarihlerini takip edin\n" +
                "✅ Bildirimlerle yenilemelerden önce uyarı alın\n" +
                "✅ Bütçenizi kontrol edin\n" +
                "✅ Farklı para birimlerinde görüntüleyin\n\n" +
                "Kısacası, aboneliklerinizi unutmadan yönetmenize yardımcı olur! 😊"
            }
            
            // Abonelik ekleme - Detaylı
            containsAny(lowerQuestion, listOf("abonelik ekle", "add subscription", "yeni abonelik", "new subscription", "nasıl eklerim", "how to add", "ekleme", "adding", "abonelik nasıl eklenir")) -> {
                "Abonelik eklemek çok kolay! 📝 İşte adım adım:\n\n" +
                "1️⃣ Ana ekranda alt navigasyon çubuğundaki + (artı) butonuna basın\n" +
                "2️⃣ Önce faturalama periyodunu seçin: Aylık veya Yıllık\n" +
                "3️⃣ Abonelik adını girin (ör: Netflix, Spotify Premium)\n" +
                "4️⃣ Ücreti girin (sadece sayı, örn: 99.99)\n" +
                "5️⃣ Yenileme tarihini yyyy-MM-dd formatında girin (ör: 2024-12-31)\n" +
                "6️⃣ Kaydet butonuna basın\n\n" +
                "💡 İpucu: İsim sadece harf ve boşluk içermeli, ücret sadece sayı olmalıdır. Hata mesajları alanların altında görünür."
            }
            
            // Abonelik silme/düzenleme
            containsAny(lowerQuestion, listOf("abonelik sil", "delete subscription", "abonelik silme", "remove subscription", "kaldır", "remove")) -> {
                "Şu anda abonelik silme özelliği geliştirilme aşamasında. 📝\n\n" +
                "Yakında eklenen abonelikleri silip düzenleyebileceksiniz. " +
                "Şimdilik, abonelik ekleme ve listeleme özelliklerini kullanabilirsiniz.\n\n" +
                "Bu özellik yakında eklenecek! ⭐"
            }
            
            containsAny(lowerQuestion, listOf("abonelik düzenle", "edit subscription", "değiştir", "change", "güncelle", "update")) -> {
                "Abonelik düzenleme özelliği yakında eklenecek! ✏️\n\n" +
                "Şu anda aboneliklerinizi ekleyebilir ve listeleyebilirsiniz. " +
                "Düzenleme ve silme özellikleri Premium sürümde daha fazlası var! ⭐\n\n" +
                "Güncellemeler için uygulamayı takip edin."
            }
            
            // Periyot (Aylık/Yıllık) soruları
            containsAny(lowerQuestion, listOf("periyot", "period", "aylık", "yıllık", "monthly", "yearly", "faturalama", "billing", "ne zaman yenileniyor")) -> {
                "Subtracky iki faturalama periyodu destekler: 📅\n\n" +
                "📆 Aylık: Her ay yenilenen abonelikler\n" +
                "   Örnek: Netflix, Spotify, Disney+\n\n" +
                "📆 Yıllık: Yılda bir kez yenilenen abonelikler\n" +
                "   Örnek: iCloud Storage, Adobe Creative Cloud\n\n" +
                "Abonelik eklerken periyodu seçmeniz gerekir. " +
                "Son seçtiğiniz periyot bir sonraki eklemede otomatik seçilir, böylece hızlıca ekleyebilirsiniz! ⚡"
            }
            
            // Abonelik alanları (isim, fiyat, tarih)
            containsAny(lowerQuestion, listOf("isim", "name", "abonelik adı", "subscription name", "nasıl yazmalıyım")) -> {
                "Abonelik ismi sadece harf ve boşluk içerebilir. 📝\n\n" +
                "✅ Doğru örnekler:\n" +
                "• Netflix\n" +
                "• Spotify Premium\n" +
                "• iCloud Storage\n" +
                "• Adobe Creative Cloud\n\n" +
                "❌ Yanlış örnekler:\n" +
                "• Netflix 4K (sayı içeriyor)\n" +
                "• Spotify-Premium (tire içeriyor)\n\n" +
                "Sayı veya özel karakter kullanamazsınız."
            }
            
            containsAny(lowerQuestion, listOf("fiyat", "price", "ücret", "tutar", "cost", "ne kadar")) -> {
                "Abonelik ücreti sadece sayı olmalıdır. 💰\n\n" +
                "✅ Doğru örnekler:\n" +
                "• 99.99\n" +
                "• 150\n" +
                "• 49.50\n\n" +
                "Para birimi sembolü (₺, $, €) otomatik eklenir, siz sadece sayıyı girin.\n\n" +
                "Seçtiğiniz para birimi (Ayarlar > Para Birimi) tüm aboneliklerde görüntülenir."
            }
            
            containsAny(lowerQuestion, listOf("tarih", "date", "yenileme tarihi", "renewal date", "ne zaman", "when", "tarih formatı")) -> {
                "Yenileme tarihi yyyy-MM-dd formatında olmalıdır. 📅\n\n" +
                "✅ Örnek: 2024-12-31\n\n" +
                "Geçerli bir tarih girmelisiniz:\n" +
                "• Ay: 1-12 arası\n" +
                "• Gün: 1-31 arası\n" +
                "• Yıl: 2020 ve sonrası\n\n" +
                "Bu tarih, abonelik yenileme hatırlatmaları için kullanılır. " +
                "Bildirimler açıksa, bu tarihten önce hatırlatma alırsınız! 🔔"
            }
            
            // Bildirimler - Detaylı
            containsAny(lowerQuestion, listOf("bildirim", "notification", "hatırlatma", "reminder", "uyarı", "alert", "nasıl açılır")) -> {
                "Bildirimler sayesinde aboneliklerinizin yenilenmesinden önce uyarı alırsınız! 🔔\n\n" +
                "Nasıl aktif edilir:\n" +
                "1️⃣ Ayarlar ekranına gidin (alt navigasyon çubuğundaki ⚙️ ikonu)\n" +
                "2️⃣ \"Bildirimler\" bölümüne gidin\n" +
                "3️⃣ Switch'i açın (Android 13+ için izin gerekir)\n\n" +
                "Hatırlatma süresi:\n" +
                "• Ücretsiz: 7 gün önceden hatırlatma\n" +
                "• Premium: 1, 3 veya 7 gün önceden seçebilirsiniz\n\n" +
                "Hatırlatma süresini değiştirmek için: Ayarlar > Faturalama Döngüsü Hatırlatıcısı"
            }
            
            // Bildirim izni sorunları
            containsAny(lowerQuestion, listOf("bildirim gelmiyor", "notification not working", "izin", "permission", "bildirim açılmıyor", "bildirim çalışmıyor")) -> {
                "Bildirim sorununu çözmek için şu adımları deneyin: 🔧\n\n" +
                "1️⃣ Ayarlar > Bildirimler bölümüne gidin\n" +
                "2️⃣ Switch'in açık olduğundan emin olun\n" +
                "3️⃣ Android 13+ kullanıyorsanız, izin isteği geldiğinde \"İzin Ver\" seçin\n" +
                "4️⃣ Eğer izin reddedildiyse:\n" +
                "   • Cihaz Ayarları > Uygulamalar > Subtracky > İzinler\n" +
                "   • Bildirimler iznini manuel olarak açın\n" +
                "5️⃣ Bildirimler açık olduğu halde gelmiyorsa:\n" +
                "   • Cihaz bildirim ayarlarını kontrol edin\n" +
                "   • Uygulamayı yeniden başlatın\n\n" +
                "Hala sorun varsa, cihazınızı yeniden başlatmayı deneyin."
            }
            
            // Dil seçimi - Detaylı
            containsAny(lowerQuestion, listOf("dil", "language", "türkçe", "english", "deutsch", "russian", "french", "spanish", "italian", "portuguese", "arabic", "chinese", "dil değiştir")) -> {
                "Subtracky 10 dil destekler! 🌍\n\n" +
                "Desteklenen diller:\n" +
                "🇹🇷 Türkçe\n" +
                "🇺🇸 English\n" +
                "🇩🇪 German\n" +
                "🇷🇺 Russian\n" +
                "🇫🇷 French\n" +
                "🇪🇸 Spanish\n" +
                "🇮🇹 Italian\n" +
                "🇧🇷 Portuguese\n" +
                "🇸🇦 Arabic\n" +
                "🇨🇳 Chinese\n\n" +
                "Dil değiştirmek için:\n" +
                "1️⃣ Ayarlar > Dil bölümüne gidin\n" +
                "2️⃣ İstediğiniz dili seçin\n" +
                "3️⃣ Uygulama otomatik olarak yeniden başlar ve yeni dil aktif olur\n\n" +
                "💡 Not: Dil değişikliği tüm uygulama arayüzünü etkiler."
            }
            
            // Tema - Detaylı
            containsAny(lowerQuestion, listOf("tema", "theme", "koyu", "açık", "dark", "light", "renk", "color", "görünüm", "appearance", "tema değiştir")) -> {
                "Subtracky 3 tema seçeneği sunar: 🎨\n\n" +
                "🌞 Açık Tema: Beyaz arka plan, koyu metin\n" +
                "   Gündüz kullanım için ideal\n\n" +
                "🌙 Koyu Tema: Siyah arka plan, açık metin\n" +
                "   Gece kullanım için ideal, göz yormaz\n\n" +
                "⚙️ Sistem: Cihazınızın tema ayarını takip eder\n" +
                "   Otomatik geçiş yapar (gündüz/gece)\n\n" +
                "Değiştirmek için:\n" +
                "1️⃣ Ayarlar > Görünüm > Tema bölümüne gidin\n" +
                "2️⃣ İstediğiniz temayı seçin\n\n" +
                "Tema değişikliği anında uygulanır, uygulamayı yeniden başlatmanıza gerek yok! ⚡"
            }
            
            // Para birimi - Detaylı
            containsAny(lowerQuestion, listOf("para birimi", "currency", "tl", "dolar", "dollar", "euro", "eur", "usd", "gbp", "jpy", "para birimi değiştir")) -> {
                "Subtracky 30'dan fazla para birimi destekler! 💰\n\n" +
                "Popüler para birimleri:\n" +
                "₺ TL (Turkish Lira)\n" +
                "$ USD (US Dollar)\n" +
                "€ EUR (Euro)\n" +
                "£ GBP (British Pound)\n" +
                "¥ JPY (Japanese Yen)\n" +
                "ve daha fazlası...\n\n" +
                "Para birimi değiştirmek için:\n" +
                "1️⃣ Ayarlar > Para Birimi bölümüne gidin\n" +
                "2️⃣ İstediğiniz para birimini seçin\n" +
                "3️⃣ Seçim anında tüm abonelik tutarlarında görüntülenir\n\n" +
                "💡 Not: Para birimi değişikliği mevcut aboneliklerin görüntülenmesini etkiler, ancak fiyatları dönüştürmez (sadece sembol değişir)."
            }
            
            // Premium - Çok detaylı
            containsAny(lowerQuestion, listOf("premium", "ücretli", "paid", "fiyat", "price", "satın al", "purchase", "paket", "package", "premium nedir")) -> {
                "Premium ile tüm özelliklerin kilidini açın! ⭐\n\n" +
                "Premium Özellikler:\n" +
                "✅ 1 ve 3 günlük hatırlatmalar (ücretsiz sadece 7 gün)\n" +
                "✅ Gelişmiş ayarlar ve özellikler\n" +
                "✅ Öncelikli destek\n\n" +
                "Premium Paketler:\n" +
                "📦 Aylık: ₺50/ay (abonelik)\n" +
                "📦 3 Aylık: ₺100/toplam (abonelik, %33 indirim)\n" +
                "📦 Yıllık: ₺150/yıl (abonelik, %75 indirim) ⭐ En Popüler\n" +
                "📦 Ömür Boyu: ₺250/tek seferlik (abonelik değil, bir kere öde)\n\n" +
                "Premium'a geçmek için:\n" +
                "1️⃣ Ayarlar ekranındaki \"Premium'a Yükselt\" kartına tıklayın\n" +
                "2️⃣ Veya ana ekrandaki Premium butonuna basın\n" +
                "3️⃣ İstediğiniz paketi seçin ve satın alın\n\n" +
                "💳 Google Play Billing ile güvenli ödeme yapılır."
            }
            
            // Premium farkları
            containsAny(lowerQuestion, listOf("premium fark", "premium difference", "ne fark var", "what's the difference", "ücretsiz vs premium", "fark nedir")) -> {
                "Ücretsiz vs Premium karşılaştırması: 📊\n\n" +
                "🆓 ÜCRETSİZ:\n" +
                "• 7 gün önceden hatırlatma\n" +
                "• Temel özellikler\n" +
                "• Sınırsız abonelik ekleme\n" +
                "• Sınırsız AI desteği\n\n" +
                "⭐ PREMIUM:\n" +
                "• 1, 3 veya 7 gün önceden hatırlatma seçeneği\n" +
                "• Tüm gelişmiş özellikler\n" +
                "• Öncelikli destek\n" +
                "• Yakında gelecek özelliklere erken erişim\n\n" +
                "Premium, abonelik yönetimini daha kolay ve verimli hale getirir! 🚀"
            }
            
            // Ayarlar ekranı
            containsAny(lowerQuestion, listOf("ayarlar", "settings", "config", "yapılandırma", "configuration", "ayarlar nerede", "where is settings")) -> {
                "Ayarlar ekranında şu bölümler bulunur: ⚙️\n\n" +
                "🎨 Görünüm: Tema seçimi (Açık/Koyu/Sistem)\n" +
                "🌍 Dil: 10 dil desteği\n" +
                "💰 Para Birimi: 30+ para birimi seçeneği\n" +
                "🔔 Bildirimler: Bildirim açma/kapama\n" +
                "📅 Faturalama Döngüsü Hatırlatıcısı: Hatırlatma süresi ayarı\n\n" +
                "Ayarlar ekranına gitmek için:\n" +
                "Ana ekranda alt navigasyon çubuğundaki ⚙️ (Ayarlar) ikonuna basın."
            }
            
            // Sorun çözme - Genel
            containsAny(lowerQuestion, listOf("sorun", "problem", "hata", "error", "çalışmıyor", "not working", "nasıl düzeltirim", "how to fix", "yardım lazım")) -> {
                "Sorun çözmek için şu adımları deneyin: 🔧\n\n" +
                "1️⃣ Uygulamayı kapatıp yeniden açın\n" +
                "2️⃣ Cihazınızı yeniden başlatın\n" +
                "3️⃣ Uygulama güncellemelerini kontrol edin (Google Play Store)\n" +
                "4️⃣ Sorun devam ederse, hangi özellikte sorun yaşıyorsunuz?\n" +
                "   • Abonelik ekleme\n" +
                "   • Bildirimler\n" +
                "   • Ayarlar\n" +
                "   • Başka bir şey\n\n" +
                "Daha spesifik yardım için sorununuzu detaylı anlatabilirsiniz. Size yardımcı olmaya çalışırım! 😊"
            }
            
            // Abonelik ekleme sorunları
            containsAny(lowerQuestion, listOf("abonelik eklenmiyor", "subscription not adding", "kaydet butonu", "save button", "hata veriyor", "error adding", "kaydet çalışmıyor")) -> {
                "Abonelik ekleme sorununu çözmek için: 🔍\n\n" +
                "1️⃣ Önce faturalama periyodunu (Aylık/Yıllık) seçtiğinizden emin olun\n" +
                "2️⃣ İsim alanı: Sadece harf ve boşluk kullanın (sayı veya özel karakter yok)\n" +
                "3️⃣ Fiyat alanı: Sadece sayı girin (örn: 99.99)\n" +
                "4️⃣ Tarih alanı: yyyy-MM-dd formatında girin (örn: 2024-12-31)\n" +
                "5️⃣ Tüm alanlar dolu ve geçerli olmalı\n" +
                "6️⃣ Kaydet butonu aktif olana kadar bekleyin (buton griyse form geçersizdir)\n\n" +
                "Hata mesajları alanların altında kırmızı renkte görüntülenir. Lütfen dikkatlice kontrol edin."
            }
            
            // AI Assistant hakkında
            containsAny(lowerQuestion, listOf("ai", "asistan", "assistant", "sen kimsin", "who are you", "ne yapabilirsin", "what can you do", "sen ne işe yarıyorsun")) -> {
                "Ben Subtracky'nin AI Asistanıyım! 🤖\n\n" +
                "Size şu konularda yardımcı olabilirim:\n" +
                "📝 Abonelik ekleme ve yönetimi\n" +
                "⚙️ Ayarlar (dil, tema, para birimi)\n" +
                "🔔 Bildirimler ve hatırlatmalar\n" +
                "⭐ Premium özellikler ve paketler\n" +
                "❓ Sorun çözme ve rehberlik\n\n" +
                "Her zaman yardımcı olmaya hazırım! 😊 " +
                "Sınırsız soru sorabilirsiniz, hiçbir limit yok!"
            }
            
            // Bütçe takibi
            containsAny(lowerQuestion, listOf("bütçe", "budget", "toplam", "total", "harcama", "spending", "ne kadar", "how much", "toplam harcama")) -> {
                "Bütçe takibi özelliği yakında eklenecek! 📊\n\n" +
                "Şu anda aboneliklerinizi listeleyebilir ve takip edebilirsiniz. " +
                "Yakında şu özellikler gelecek:\n" +
                "• Aylık/yıllık toplam harcama görüntüleme\n" +
                "• Kategorilere göre dağılım\n" +
                "• Bütçe analizi ve raporlar\n\n" +
                "Bu özellikler Premium sürümde daha fazlası var! ⭐\n\n" +
                "Güncellemeler için uygulamayı takip edin."
            }
            
            // Genel sohbet (Daha doğal)
            containsAny(lowerQuestion, listOf("teşekkür", "thanks", "thank you", "sağol", "sağ ol")) -> {
                "Rica ederim! 😊 Başka bir konuda yardımcı olabilir miyim? Subtracky hakkında merak ettiğiniz başka bir şey varsa çekinmeyin!"
            }
            
            containsAny(lowerQuestion, listOf("tamam", "ok", "anladım", "got it", "i understand")) -> {
                "Harika! 😊 Başka bir sorunuz varsa veya yardıma ihtiyacınız olursa, her zaman buradayım!"
            }
            
            // Bilinmeyen özellikler veya gelecek özellikler
            else -> {
                "Bu konuda şu anda size yardımcı olamıyorum, ancak yakında daha akıllı hale geleceğim! ⭐\n\n" +
                "Şu anda size şu konularda yardımcı olabilirim:\n" +
                "• Abonelik ekleme ve yönetimi\n" +
                "• Ayarlar (dil, tema, para birimi)\n" +
                "• Bildirimler ve hatırlatmalar\n" +
                "• Premium özellikler\n" +
                "• Sorun çözme\n\n" +
                "Başka bir sorunuz varsa çekinmeyin! 😊"
            }
        }
    }
    
    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }
}

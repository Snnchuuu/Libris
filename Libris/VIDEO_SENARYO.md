# 🎬 Libris — Video Sunum Senaryosu

**Hedef süre:** 6-8 dakika
**Format:** Ekran kaydı + facecam (sağ alt köşede balon)
**Ses:** Mikrofondan canlı anlatım

---

## 0. HAZIRLIK (kayıt ÖNCESİ — 5 dakika)

Kayıt başlamadan ÖNCE yap:

- [ ] **DB sıfırla** (temiz demo için):
  ```bash
  mysql -u root libris_db < /Users/yigitakcay/Libris/Libris/sql/full_demo_reset.sql
  ```
- [ ] **Uygulamayı çalıştır** (önceden açık tut):
  ```bash
  cd /Users/yigitakcay/Libris/Libris && mvn spring-boot:run
  ```
- [ ] **Tarayıcı sekmeleri** hazır olsun:
  - Sekme 1: `http://localhost:8080/login` (üye için)
  - Sekme 2: `http://localhost:8080/login` (admin için, sonra)
  - Sekme 3: Gmail (kullanıcının inbox'ı — mail demosu için)
- [ ] **MySQL Workbench** açık (DB'yi göstereceksen)
- [ ] **GitHub repo** sekmesi de hazır (kod vurgusu için)
- [ ] **Bildirimleri kapat** (Mac: Do Not Disturb açık)
- [ ] **Mikrofon test** et (OBS'de seviye barını gör)

---

## 1. INTRO — 30 saniye

**[Facecam'i tam ekran yap]** veya **[küçük facecam + Libris logosu ekran]**

> "Merhaba, ben Yiğit. Bugün size grubumuzla geliştirdiğimiz **Libris**, kütüphane yönetim sistemini tanıtacağım. Java + Spring Boot + Vaadin + MySQL ile yazıldı. Hem üye hem admin tarafından kullanılabilen, ödünç alma, ceza sistemi, rezervasyon, istek listesi ve değerlendirmeler gibi özellikleri olan bir uygulama. Hadi başlayalım."

⏱️ **0:30**

---

## 2. TEKNOLOJİ STACK — 30 saniye

**[Ekran: VS Code veya GitHub repo aç, src/main/java/com/libris/ klasörünü göster]**

> "Backend tarafında Spring Boot ve JDBC ile MySQL'e bağlanıyoruz. UI için Vaadin Flow kullandık — Java sınıflarıyla web arayüzü yazmamızı sağlıyor. Veritabanında 5 ana tablo var: users, library_items, borrow_records, reservations, reviews ve wish_list. DAO sınıflarıyla her tabloyu kapsülledik. Kod tabanı GitHub'da `Snnchuuu/Libris` reposu altında."

**[Hızlıca BookDAO.java, BorrowDAO.java, MyBorrowedView.java dosyalarını listele]**

⏱️ **1:00**

---

## 3. KAYIT OL — 45 saniye

**[Ekran: tarayıcı, http://localhost:8080/register]**

> "Önce yeni bir üye kaydı yapalım. Kayıt formumuzda 8 katmanlı doğrulama var."

**[Boş bırak + Kayıt Ol tıkla]** → "Lütfen tüm alanları doldurun"

> "Boş alan kontrolü."

**[Username = "ab"]** → kırmızı "Geçersiz kullanıcı adı"

> "Kullanıcı adı en az 3 karakter olmalı."

**[Email = "abc"]** → "E-posta geçerli değil"

> "Email formatı RFC standardına uygun olmalı."

**[Doğru bilgilerle doldur:]**
- Username: `demouser`
- Ad Soyad: `Demo Kullanıcı`
- Email: `demo@libris.test`
- Şifre: `demo123`

**[Kayıt Ol → "Hesabın oluşturuldu" mesajı + login sayfasına yönlendiriliyor]**

⏱️ **1:45**

---

## 4. GİRİŞ + KATALOG İLK İZLENİM — 45 saniye

**[Email + şifre ile giriş yap]**

> "Email ve şifremle giriş yapıyorum."

**[Katalog sayfası açıldı]**

> "İşte ana katalog sayfası. Üst köşede logom ve hoşgeldin mesajım, yanında bakiye göstergesi (şu an 0 TL — yeşil), Ödünç Aldıklarım, Rezervasyonlarım, İstek Listem ve çıkış butonu var.
> Tabloda kitaplar listelenmiş; her satırda kitap adı, yazar, yayın yılı, durum, tür, stok, ortalama puan, Yorumlar butonu, Ödünç Al butonu ve kalp ikonu (istek listesi) var."

**[Tabloyu tepeden tırnağa scroll et, kolonları göster]**

⏱️ **2:30**

---

## 5. KİTAP ÖDÜNÇ AL — 45 saniye

**[Java Programming → Ödünç Al]** → "✅ ödünç alındı, iade tarihi 3 dakika sonra"

> "Java Programming kitabını ödünç alıyorum. Test modunda olduğumuz için ödünç süresi 3 dakika — gerçek sistemde günlük olur."

**[Hemen Gmail sekmesine geç]**

> "Onay e-postası geldi mi bakalım..."

**[Gmail'i yenile, ödünç onay maili gözüksün]**

> "İşte! 'Ödünç Alma Onayı — Java Programming'. Mail içinde kitap adı, vade tarihi ve saati, hatta iade sonrası değerlendirme yazma daveti var."

**[Maili göster, kapat]**

⏱️ **3:15**

---

## 6. ÖDÜNÇ ALDIKLARIM SAYFASI — 30 saniye

**[Header'da "Ödünç Aldıklarım" → tıkla]**

> "Burada aktif ödünçlerim ve geçmiş kayıtlarım ayrı tablolarda görünüyor. **Aktif** tabloda canlı bir geri sayım var — Kalan Süre kolonu her saniye güncelleniyor. Yanında 'Olası Ceza' kolonu da var; süre dolduğunda dakika başına 1 TL ceza birikecek."

**[Geri sayım çalışırken birkaç saniye bekle, sayacın değiştiğini göster]**

⏱️ **3:45**

---

## 7. WISHLIST + REZERVASYON — 60 saniye

**[Kataloğa dön]**

> "Şimdi istek listesi özelliği. Bir kitabın yanındaki kalp ikonuna tıklıyorum..."

**[Sapiens'in kalp ikonuna tıkla → kalp pembeleşir]** → "İstek listesine eklendi"

**[Header'dan "İstek Listem" → liste göster]**

> "İstek listemde gördüğüm kitabı buradan da ödünç alabilir veya çıkarabilirim. Eğer stok yoksa Rezerve Et butonu gözüküyor."

**[Kataloğa dön → stoğu 0 olan bir kitap olduğunu varsay (veya yapay 0'lat)]**
**[Rezerve Et butonuna tıkla]** → "✅ Rezervasyon oluşturuldu"

> "Rezervasyon yaptığım kitap stoğa girdiğinde otomatik mail alacağım."

**[Header'dan "Rezervasyonlarım" → bekleyen rezervasyonu göster, iptal butonunu vurgula]**

⏱️ **4:45**

---

## 8. ZAMAN ATLAMASI — GECİKTİR — 30 saniye

**[Ekran: 3 dakika geçmesini bekle veya zaten geçmiş gibi yap]**

> "Şimdi 3 dakika geçti, kitabı iade etmedim. Sistem otomatik olarak gecikme cezası uygulayacak ve bana mail atacak."

**[Gmail'e geç, "ACİL: İade Süreniz Doldu" mailini göster]**

> "İşte uyarı maili — kitabın adı, vade tarihi, ve hızlı iade çağrısı var."

**[Ödünç Aldıklarım sayfasına dön — aktif satır kırmızıya boyanmış, "GECİKTİ — X dakika"]**

> "Tabloda da satır kırmızıya boyandı ve gecikme süresi gösteriliyor. Olası Ceza kolonunda artan ceza."

⏱️ **5:15**

---

## 9. İADE + CEZA + DEĞERLENDİRME — 45 saniye

**[İade Et butonuna tıkla]**

> "İade ediyorum..."

**[Bildirim: "İade alındı: Java Programming — Gecikme cezası: X TL (Bakiye: X TL)"]**

> "İade alındı, X TL ceza uygulandı, bakiyeme eklendi."

**[Otomatik review dialog'u açılır — yıldız + yorum]**

> "Sistem ayrıca değerlendirme istiyor. 5 yıldız veriyorum, 'Çok faydalı bir Java kitabı' yazıp gönderiyorum."

**[Gönder → kataloğa dön → Java Programming'in 'Puan' kolonu ⭐ 5.0 (1) olmuş]**

**[Kataloğun "Yorumlar" butonuna tıkla → yorumum görünüyor]**

> "Diğer üyeler de yazdığım yorumu görebilir."

⏱️ **6:00**

---

## 10. CEZAYI ÖDE — 20 saniye

**[Header'da bakiye kırmızı + "Cezayı Öde" butonu]**

> "Header'da bakiyem kırmızıyla X TL gözüküyor, 'Cezayı Öde' butonuna basıyorum..."

**[Onay dialog → Öde]** → bakiye 0'a iner, etiket yeşile döner

> "Ödedim, bakiyem sıfırlandı."

⏱️ **6:20**

---

## 11. ADMIN PANELİ — 45 saniye

**[Çıkış yap, admin olarak giriş yap: ahmet@libris.com / admin123]**

> "Şimdi admin tarafına bakalım."

**[Ekran: katalog admin gözüyle — Stok Güncelle, Sil, Ekle butonları görünür]**

> "Admin görünümünde her kitabın yanında 'Stoğu Güncelle' butonu var. Üstte 4 farklı tür için 'Ekle' butonları: kitap, e-kitap, sesli kitap, süreli yayın."

**[Bir kitabın stoğunu 5 → 10 yap, Güncelle → bildirimde "Stok güncellendi"]**

> "Stok güncellendiğinde, eğer rezerve eden biri varsa otomatik mail atılıyor."

**[Bir test kitabı eklemek için 'Kitap Ekle' aç → form göster, ekle]**

**[Sil butonu — bir kitabı sil → cascade temizleme: borrow_records, reservations, reviews, wish_list temizleniyor]**

> "Silme işlemi cascade çalışıyor — bağlı tüm kayıtlar tek transaction'da temizleniyor."

⏱️ **7:05**

---

## 12. KAPANIŞ — 25 saniye

**[Facecam'i büyüt, ekrana logoyu veya GitHub URL'sini yansıt]**

> "İşte Libris'in temel akışı. Daha gösteremediğim özellikler de var:
> - Şifremi unuttum (mail ile yeni şifre)
> - Geç-iade scheduler her 10 saniyede bir DB'yi tarayıp uyarı maili atar
> - Veritabanı şeması otomatik migrasyonlarla kendini onarır (eski kolonları DATETIME'a çevirir, eksik tablo varsa oluşturur)
> - GitHub'da repo: Snnchuuu/Libris, email-feature branch
>
> Beni dinlediğiniz için teşekkür ederim. Sorularınız için iletişime geçebilirsiniz."

⏱️ **7:30**

---

## 🎯 İPUÇLARI

- **Konuşurken** maus hareketin yavaş olsun, izleyici takip edebilsin
- **Kısa duraklamalar ver** — özellikle bir buton tıkladıktan sonra sonucun gözükmesini bekle
- **Tek tek özellikleri vurgula** — "şimdi şuna bakalım" gibi geçişler yap
- **Hata olursa kayıt durdurma** — devam et, montajda kesersin
- **Kayıt sonrası iMovie veya DaVinci Resolve** ile gereksiz yerleri kes, başına intro/sonuna outro ekle (logo + müzik)
- **Çözünürlük 1080p**, **30 fps** yeterli (üzeri dosya boyutunu büyütür)
- **Mikrofonu** facecam'den uzakta ama net duyulacak şekilde tut

## 🎬 KAYIT ÖNCESİ FİNAL CHECKLİST

- [ ] DB sıfırlanmış mı (full_demo_reset.sql)
- [ ] Uygulama çalışıyor mu (`localhost:8080` açık)
- [ ] Gmail'e giriş yapılı mı (mail demosu için)
- [ ] Mac "Do Not Disturb" açık mı
- [ ] OBS/Loom kayıtta mı
- [ ] Mikrofon seviyesi OK mı
- [ ] Facecam aydınlatma OK mı
- [ ] Su/çay yanında mı 😄

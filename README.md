# AquaFishing 🎣

**AquaFishing**, modern Minecraft (Paper 1.21.x) sunucuları için geliştirilmiş, yüksek performanslı, nesne yönelimli (OOP) ve tam donanımlı bir **RPG Balıkçılık & Turnuva** eklentisidir.

Klasik vanilya balık tutma mekaniğini; 40 adet özel pikselli balık türü, 10 kademeli RPG oltaları, beceri odaklı yeşil bar mini-oyunu (timing minigame), balık ansiklopedisi (Codex) ve balıkçı pazarı ile zengin bir etkinliğe dönüştürür.

---

## ✨ Öne Çıkan Özellikler

### 🐟 40 Adet Özel Balık Türü & Nadirlik Seviyeleri
- **4 Farklı Nadirlik**: `Yaygın (Common)`, `Nadir (Rare)`, `Epik (Epic)`, `Efsanevi (Legendary)`.
- Her balık için özel 32x32 piksel art dokusu, ağırlık havuzu (kg), özel lore ve `PersistentDataContainer` (PDC) kimlik doğrulaması.
- **Koleksiyon & Ansiklopedi (Codex)**: `/fish book` komutu ile tutulan balık türleri, yakalanma sayıları ve kırılan kişisel rekorlar kaydedilir.

### 🎣 10 Kademeli Özel RPG Oltaları
Her biri benzersiz piksel dokuya ve özel niteliklere (Yeşil Bar Gücü, Sarma Hızı, Vuruş Hızı, Nadir Balık Şansı) sahip 10 kademe:
1. **Çırak Tahta Oltası** (Kademe I)
2. **Bambu Nehir Oltası** (Kademe II)
3. **Takviyeli Demir Olta** (Kademe III)
4. **Kızıltaş Mekanik Olta** (Kademe IV)
5. **Saf Altın Bereket Oltası** (Kademe V)
6. **Derin Deniz Ametist Oltası** (Kademe VI)
7. **Prizmarin Okyanus Oltası** (Kademe VII)
8. **Usta Elmas Olta** (Kademe VIII)
9. **Netherite Magma Oltası** (Kademe IX)
10. **Melon Kadim Ejder Oltası** (Kademe X - Mitolojik)

### 🎯 Dinamik Sarma Mini-Oyunu (Timing Minigame)
- Balık oltaya vurduğunda Action Bar üzerinde dinamik bir bar çıkar.
- Bar üzerindeki yeşil hedef alanına imleç denk geldiğinde sağ tıklayarak balığı çekme mekaniği.
- Daha üst kademe oltalar yeşil alanı genişletir ve sarma hızını artırır.

### 💰 Doğrudan Satış & Balıkçı Pazarı
- **`/fish sell`**: Envanterdeki tüm turnuva balıklarını tek komutla değerine göre doğrudan Altın Külçesi'ne çevirir.
- **`/fish shop`**: Oltaların gereksinimlerini (gerekli av sayısı, önceki kademe olta ve altın) inceleyip kademe atlama GUI menüsü.

### 🏆 Otomatik & Canlı Turnuva Sistemi
- Otomatik periyodik turnuva veya yönetici komutuyla anlık başlatma.
- Canlı **BossBar** göstergesi (kalan süre, lider ve en ağır balık).
- Yeni rekor duyuruları ve ilk 3'e giren oyunculara özelleştirilebilir ödüller.

### 📦 Dahili Kaynak Paketi Dağıtıcısı (Built-in HTTP Pack Server)
- Eklenti, port 25570 üzerinden dahili bir HTTP sunucusu açarak `AquaFishing-Pack.zip` paketini oyunculara otomatik ve güvenli (SHA-1 hash doğrulamalı) olarak iletir.

---

## 📜 Komutlar & Yetkiler

| Komut | Açıklama | Varsayılan Yetki |
| :--- | :--- | :--- |
| `/fish sell` | Envanterdeki tüm turnuva balıklarını doğrudan satar | Herkese açık |
| `/fish shop` | Balıkçı pazarını ve olta geliştirme menüsünü açar | Herkese açık |
| `/fish book` | Balıkçı ansiklopedisini (Codex GUI) açar | Herkese açık |
| `/fish top` | Anlık turnuva liderlik tablosunu gösterir | Herkese açık |
| `/fish help` | Komut listesini gösterir | Herkese açık |
| `/fish start [süre]` | Turnuvayı başlatır (Örn: `/fish start 300`) | `aquafishing.admin` |
| `/fish stop` | Devam eden turnuvayı erkenden bitirir | `aquafishing.admin` |
| `/fish give rod <id>` | Belirtilen özel oltayı verir | `aquafishing.admin` |
| `/fish give fish <id>` | Belirtilen özel balığı verir | `aquafishing.admin` |
| `/fish reload` | Eklenti konfigürasyonunu yeniler | `aquafishing.admin` |

---

## 🛠️ Derleme & Kurulum

```bash
# Projeyi derlemek için:
./gradlew build
```
Derlenen jar dosyası `build/libs/AquaFishing-1.0.0-SNAPSHOT.jar` konumunda oluşur.
Sunucunuzun `plugins/` klasörüne atıp yeniden başlatmanız yeterlidir.

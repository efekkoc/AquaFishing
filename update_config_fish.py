import re

fish_data = [
    # Row 1
    ("morina", "Morina", 10001, "COMMON", 2.0, 7.0, "Soğuk denizlerin klasik, iri yapılı morinası."),
    ("somon", "Somon", 10002, "COMMON", 1.5, 5.5, "Nehir akıntılarına karşı yüzen lezzetli pembe somon."),
    ("balon_baligi", "Balon Balığı", 10003, "COMMON", 0.5, 3.0, "Tehlike hissettiğinde kendini şişiren dikenli balık."),
    ("tropikal_balik", "Tropikal Balık", 10004, "COMMON", 0.3, 2.0, "Mercan resiflerinde süzülen rengarenk egzotik balık."),
    ("yayin_baligi", "Yayın Balığı", 10005, "RARE", 8.0, 25.0, "Nehir diplerinde pusu kuran bıyıklı dev yayın balığı."),
    ("turna_baligi", "Turna Balığı", 10006, "RARE", 4.0, 14.0, "Keskin dişleri ve hızlı saldırılarıyla ünlü tatlı su avcısı."),
    ("sardalya", "Sardalya", 10007, "COMMON", 0.2, 1.2, "Gümüş rengi pullarıyla parıldayan küçük sürü balığı."),
    ("koi_baligi", "Koi Balığı", 10008, "RARE", 2.0, 8.0, "Şans ve bereket getirdiğine inanılan kırmızı-beyaz desenli koi."),
    
    # Row 2
    ("alabalik", "Alabalık", 10009, "COMMON", 1.0, 4.5, "Buz gibi berrak dağ derelerinde yaşayan benekli alabalık."),
    ("pilic_baligi", "Piliç Balığı", 10010, "COMMON", 1.2, 5.0, "Kumlu diplerde yassı gövdesiyle gizlenen lezzetli dip balığı."),
    ("kilic_baligi", "Kılıç Balığı", 10011, "EPIC", 15.0, 45.0, "Mızrak gibi burnuyla okyanusta jet hızıyla yüzen efsane avcı."),
    ("pirana", "Pirana", 10012, "RARE", 0.5, 2.5, "Amazon sularının jilet gibi dişlere sahip vahşi yırtıcısı."),
    ("fener_baligi", "Fener Balığı", 10013, "EPIC", 6.0, 18.0, "Karanlık okyanus diplerinde ışığıyla avını büyüleyen derin deniz canavarı."),
    ("yilan_baligi", "Yılan Balığı", 10014, "RARE", 2.0, 9.0, "Kıvrılarak yüzen, yakalaması son derece zor kaygan bir av."),
    ("deniz_ati", "Deniz Atı", 10015, "RARE", 0.2, 1.0, "Deniz çayırlarına kuyruğuyla tutunan minik ve zarif deniz atı."),
    ("vatoz", "Vatoz", 10016, "RARE", 5.0, 22.0, "Okyanus tabanında kanat çırpar gibi süzülen elektrik mavisi vatoz."),
    
    # Row 3
    ("altin_sazan", "Altın Sazan", 10017, "LEGENDARY", 10.0, 35.0, "Güneş gibi parlayan altın aurasıyla bolluk saçan efsanevi sazan."),
    ("lav_baligi", "Lav Balığı", 10018, "LEGENDARY", 12.0, 40.0, "Volkanik magma kraterlerinde yaşayan, derisi alev alev yanan balık."),
    ("ejder_balik", "Ejder Balık", 10019, "LEGENDARY", 15.0, 50.0, "Mor kozmik parıltılar saçan, kadim ejderha soyundan gelen mitolojik yaratık."),
    ("cerrah_baligi", "Cerrah Balığı", 10020, "COMMON", 0.8, 3.2, "Koyu mavi gövdesi ve sarı kuyruğuyla resiflerin neşesi."),
    ("kaplan_baligi", "Kaplan Balığı", 10021, "RARE", 4.0, 15.0, "Kaplan çizgili zırhı ve yırtıcı gücüyle nehirlerin hükümdarı."),
    ("isik_baligi", "Işık Balığı", 10022, "LEGENDARY", 5.0, 20.0, "Derinlerde etrafına neon mor ışık hüzmeleri yayan mucizevi varlık."),
    ("aksulot", "Aksulot", 10023, "EPIC", 1.0, 4.0, "Yeraltı göllerinde gülümseyen sevimli ve nadide amfibi dost."),
    ("kopek_baligi", "Köpek Balığı", 10024, "LEGENDARY", 30.0, 120.0, "Okyanusların korkulu rüyası, devasa dişlere sahip tepe avcı."),
    
    # Row 4
    ("aslan_baligi", "Aslan Balığı", 10025, "EPIC", 2.0, 7.5, "Göz alıcı yelpaze yüzgeçleri ve zehirli iğneleriyle gösterişli av."),
    ("orkinos", "Orkinos", 10026, "EPIC", 25.0, 90.0, "Derin denizlerin saf kas gücüyle dolu en değerli ton balığı."),
    ("iskender_baligi", "İskender Balığı", 10027, "RARE", 3.0, 11.0, "Gümüşi zırhı ve asil duruşuyla bilinen lezzetli av."),
    ("ucan_balik", "Uçan Balık", 10028, "RARE", 0.5, 2.2, "Dalgaların üzerinden metrelerce havalanıp süzülen kanatlı balık."),
    ("zirhli_balik", "Zırhlı Balık", 10029, "EPIC", 8.0, 30.0, "Taş gibi sert plakalarla kaplı tarih öncesinden kalma canlı fosil."),
    ("dev_murekkep", "Dev Mürekkep", 10030, "LEGENDARY", 40.0, 150.0, "Gemi batıran dev dokunaçlara sahip okyanus derinliklerinin krakeni."),
    ("buz_baligi", "Buz Balığı", 10031, "EPIC", 2.5, 9.0, "Kutup buzullarının altında yaşayan, şeffaf kristal donmuş balık."),
    ("yildiz_baligi", "Yıldız Balığı", 10032, "LEGENDARY", 1.0, 5.0, "Gökyüzündeki takımyıldızlar gibi ışıldayan sihirli yıldız balığı."),
    
    # Row 5
    ("balgam_baligi", "Balgam Balığı", 10033, "LEGENDARY", 6.0, 24.0, "Radyoaktif yeşil slime aurası yayan, akışkan ve gizemli bir yaratık."),
    ("hayalet_balik", "Hayalet Balık", 10034, "EPIC", 3.0, 12.0, "Suların arasından bir ruh gibi süzülen mavi ışıklı hayalet balık."),
    ("kutsal_balik", "Kutsal Balık", 10035, "LEGENDARY", 8.0, 32.0, "Kırmızı pullarıyla antik tapınaklarda korunan kutsal ejder balığı (Arowana)."),
    ("devasa_balik", "Devasa Balık", 10036, "LEGENDARY", 50.0, 200.0, "Oltayı kıracak güçte asılan, okyanusun en ağır devi."),
    ("palyaco_baligi", "Palyaço Balığı", 10037, "COMMON", 0.3, 1.8, "Anemonların arasında saklanan turuncu-beyaz çizgili sevimli balık."),
    ("deniz_anasi", "Deniz Anası", 10038, "EPIC", 1.5, 6.0, "Pembe fosforlu kubbesiyle akıntıda dans eden hipnotize edici deniz anası."),
    ("kalkan_baligi", "Kalkan Balığı", 10039, "COMMON", 3.0, 12.0, "Deniz tabanında kendini mükemmel kamufle eden yuvarlak kalkan balığı."),
    ("merlin", "Merlin", 10040, "EPIC", 20.0, 70.0, "Okyanusun mavi şimşeği, devasa yelken yüzgeçli kral balık.")
]

fish_yaml_lines = ["\n# Özel Balık Türleri ve Ağırlık Havuzu (40 Çeşit Balık)", "fish-types:"]

for fid, name, cmd, rarity, min_w, max_w, desc in fish_data:
    fish_yaml_lines.append(f"  {fid}:")
    fish_yaml_lines.append(f"    name: \"{name}\"")
    fish_yaml_lines.append(f"    material: \"COD\"")
    fish_yaml_lines.append(f"    custom-model-data: {cmd}")
    fish_yaml_lines.append(f"    rarity: \"{rarity}\"")
    fish_yaml_lines.append(f"    min-weight: {min_w}")
    fish_yaml_lines.append(f"    max-weight: {max_w}")
    fish_yaml_lines.append("    lore:")
    fish_yaml_lines.append(f"      - \"<gray>{desc}\"")
    fish_yaml_lines.append("      - \"<dark_gray>AquaFishing Turnuva Balığı\"")

fish_section_str = "\n".join(fish_yaml_lines) + "\n"

# Update src/main/resources/config.yml
config_paths = [
    r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\src\main\resources\config.yml",
    r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\test-server\plugins\AquaFishing\config.yml"
]

for p in config_paths:
    with open(p, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Replace from 'fish-types:' to end of file
    idx = content.find("fish-types:")
    if idx != -1:
        # Find preceding comment if any
        comment_idx = content.rfind("#", 0, idx)
        if comment_idx != -1 and "Özel Balık" in content[comment_idx:idx]:
            new_content = content[:comment_idx].rstrip() + "\n" + fish_section_str
        else:
            new_content = content[:idx].rstrip() + "\n" + fish_section_str
    else:
        new_content = content.rstrip() + "\n" + fish_section_str
    
    with open(p, "w", encoding="utf-8") as f:
        f.write(new_content)
    print(f"Updated {p}")

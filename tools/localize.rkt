#lang racket

(define lang-list '((en "../app/src/main/res/values/strings.xml")
                    (ja "../app/src/main/res/values-ja-rJP/strings.xml")
                    (hu "../app/src/main/res/values-hu-rHU/strings.xml")
                    (fr "../app/src/main/res/values-fr-rFR/strings.xml")))

(define value-list
  '(("app_name"
     ((* . "Clock with Planisphere")))
    ("locationSettings"
     ((en . "Location Settings")
      (ja . "観測地設定")
      (hu . "Megfigyelési hely beállítása")
      (fr . "Paramètres du site d’observation")))
    ("license"
     ((en . "License")
      (ja . "使用許諾")
      (hu . "Licenc")
      (fr . "Licence")))
    ("opensource_licenses"
     ((en . "OpenSource Licenses")
      (ja . "オープンソースライセンス")
      (hu . "Nyílt forráskódú licencek")
      (fr . "Licences open source")))
    ("latitude"
     ((en . "Latitude: ")
      (ja . "緯度：")
      (hu . "Szélesség: ")
      (fr . "Latitude : ")))
    ("latitudeHint"
     ((* . "+23.4567")))
    ("longitude"
     ((en . "Longitude: ")
      (ja . "経度：")
      (hu . "Hosszúság: ")
      (fr . "Longitude : ")))
    ("longitudeHint"
     ((* . "+123.456")))
    ("gps"
     ((* . "GPS")))
    ("modify"
     ((en . "Modify")
      (ja . "変更する")
      (hu . "Módosítás")
      (fr . "Modifier")))
    ("cancel"
     ((en . "Cancel")
      (ja . "変更しない")
      (hu . "Mégse")
      (fr . "Annuler")))
    ("inGettingLocation"
     ((en . "in getting location…")
      (ja . "位置情報を取得中…")
      (hu . "Helyadatok lekérdezése…")
      (fr . "Obtention de la localisation…")))
    ("pleaseCheckIfGPSIsOn"
     ((en . "Please check if GPS is on.")
      (ja . "GPSがONかチェックして下さい。")
      (hu . "Ellenőrizze, hogy be van-e kapcsolva a GPS.")
      (fr . "Veuillez vérifier que le GPS est activé.")))
    ("app_license"
     ((* . "Copyright 2020-2026 Yasuhiro Yamakawa &lt;withlet11@gmail.com&gt;\\n\\n
        Permission is hereby granted, free of charge, to any person obtaining a copy of this
        software and associated documentation files (the \"Software\"), to deal in the Software
        without restriction, including without limitation the rights to use, copy, modify, merge,
        publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
        to whom the Software is furnished to do so, subject to the following conditions:\n\n
        The above copyright notice and this permission notice shall be included in all copies or
        substantial portions of the Software.\\n\\n
        THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
        INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
        PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
        FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
        OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
        DEALINGS IN THE SOFTWARE. ")))
    ("privacy_policy_header"
     ((en . "Privacy Policy")
      (ja . "プライバシポリシー")
      (hu . "Adatvédelmi irányelvek")
      (fr . "Politique de confidentialité")))
    ("privacy_policy"
     ((en . "<i>Last updated on 20 Nov 2020</i>\\n\\n
        In order to clarify what we do on this matter and how the information provided by the
        users are used is described in this privacy policy.\\n\\n
        <b>User data collection</b>\\n
        We do not collect any user information including: names, phone numbers, e-mail addresses
        and passwords. However, we use 3rd party ad service (Google AdMob™) in this app, and that
        service may collect your information. The privacy policy can be read on the following page:
            <a href=\"https://policies.google.com/privacy\">Google privacy policy</a>\\n\\n
        <b>Additional permissions</b>\\n
        This application accesses your geolocation information, only if you permit. That data
        is stored at your device only, and never provided to any other applications or services.\\n
        Internet is used to display advertisement banners.\\n\\n
        <b>Changes</b>\\n
        This Privacy Policy may be updated from time to time for any reason.\\n\\n
        <b>Your Consent</b>\\n
        By using this app you consent to this Privacy Policy as well as all the 3rd party
        Privacy Policies mentioned above. If you disagree with this Privacy Policy or the 3rd party
        Privacy Policies you can uninstall this app.\\n\\n
        <b>Contact Us</b>\\n
        If there are any questions regarding this Privacy Policy you may contact us by email:
        <a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>")
      (ja . "<u>最終更新：2020-11-20</u>\\n\\n
本アプリのご利用に当たりまして、このプライバシポリシーにて個人情報の扱いをご確認下さい。\\n\\n
<b>ユーザのデータの収集</b>\\n
本アプリの開発元は個人情報を収集しません。ただし、サードパーティの広告サービス（Google AdMob™）を利用しており、そのサービスが個人情報を収集することがあります。そのプライバシポリシーにつきましては、<a href=\"https://policies.google.com/privacy\">Google privacy polcy</a>でご確認下さい。\\n\\n
<b>追加の権限</b>\\n
本アプリは、許可が与えられた場合のみ、位置情報を利用します。そのデータが他のアプリやサービスで利用されることはありません。 広告バナーの表示のためにインターネットが利用されます。\\n\\n
<b>変更</b>\\n
このプライバシポリシーは何らかの理由により変更されることがあります。\\n\\n
<b>利用にあたっての同意</b>\\n
本プライバシポリシー及びサードパーティのプライバシポリシーに同意の上でご利用下さい。もし、同意いただけない場合は本アプリをアンインストールして下さい。\\n\\n
<b>お問い合わせ</b>\\n
プライバシポリシーにつきましてご質問がある場合は<a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>にお問い合わせ下さい。")
      (hu . "<i>Utoljára frissítve: 2020. november 20.</i>\\n\\n
        A jelen adatvédelmi irányelvek ismertetik, hogyan kezeljük a felhasználók által megadott
        információkat és hogyan használjuk fel azokat.\\n\\n
        <b>Felhasználói adatok gyűjtése</b>\\n
        Nem gyűjtünk felhasználói adatokat, például neveket, telefonszámokat, e-mail-címeket
        és jelszavakat. Az alkalmazás azonban egy külső hirdetési szolgáltatást
        (Google AdMob™) használ, amely gyűjtheti az Ön adatait. Az adatvédelmi irányelvek
        a következő oldalon olvashatók:
            <a href=\"https://policies.google.com/privacy\">Google adatvédelmi irányelvei</a>\\n\\n

        <b>További engedélyek</b>\\n
        Az alkalmazás csak az Ön engedélyével fér hozzá a helyadatokhoz. Ezeket az adatokat
        kizárólag az Ön eszközén tároljuk, és soha nem továbbítjuk más alkalmazásoknak vagy
        szolgáltatásoknak.\\n
        Az internetkapcsolatot a hirdetési bannerek megjelenítésére használjuk.\\n\\n

        <b>Módosítások</b>\\n
        A jelen adatvédelmi irányelvek időről időre, bármilyen okból módosulhatnak.\\n\\n

        <b>Az Ön hozzájárulása</b>\\n
        Az alkalmazás használatával Ön elfogadja a jelen adatvédelmi irányelveket, valamint
        a fent említett külső szolgáltatók adatvédelmi irányelveit. Ha nem ért egyet a jelen
        adatvédelmi irányelvekkel vagy a külső szolgáltatók adatvédelmi irányelveivel,
        eltávolíthatja az alkalmazást.\\n\\n

        <b>Kapcsolat</b>\\n
        Ha kérdése van a jelen adatvédelmi irányelvekkel kapcsolatban, e-mailben léphet
        kapcsolatba velünk:
        <a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>")
      (fr . "<i>Dernière mise à jour : 20 novembre 2020</i>\\n\\n
        La présente politique de confidentialité décrit la manière dont nous traitons les
        informations fournies par les utilisateurs et dont nous les utilisons.\\n\\n

        <b>Collecte des données utilisateur</b>\\n
        Nous ne collectons aucune information personnelle, notamment les noms, numéros de
        téléphone, adresses e-mail et mots de passe. Toutefois, cette application utilise un
        service publicitaire tiers (Google AdMob™), qui peut collecter vos informations.
        La politique de confidentialité de ce service est disponible sur la page suivante :
            <a href=\"https://policies.google.com/privacy\">Politique de confidentialité de Google</a>\\n\\n

        <b>Autorisations supplémentaires</b>\\n
        Cette application accède à vos données de localisation uniquement si vous l’y autorisez.
        Ces données sont stockées uniquement sur votre appareil et ne sont jamais transmises à
        d’autres applications ou services.\\n
        Internet est utilisé pour afficher les bannières publicitaires.\\n\\n

        <b>Modifications</b>\\n
        Cette politique de confidentialité peut être mise à jour périodiquement pour quelque
        raison que ce soit.\\n\\n

        <b>Votre consentement</b>\\n
        En utilisant cette application, vous acceptez la présente politique de confidentialité
        ainsi que les politiques de confidentialité des services tiers mentionnées ci-dessus.
        Si vous n’acceptez pas cette politique de confidentialité ou celles des services tiers,
        vous pouvez désinstaller cette application.\\n\\n

        <b>Nous contacter</b>\\n
        Pour toute question concernant cette politique de confidentialité, vous pouvez nous
        contacter par e-mail :
        <a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>")))
    ("north_label"
     ((* . "N")))
    ("south_label"
     ((* . "S")))
    ("ns_switch"
     ((* . "NS Switch")))
    ("no_permission_to_access_location_permanent"
     ((en . "No permission to access the location service. Please check the application permission in the system settings.")
      (ja . "位置情報へのアクセスの権限がありません。設定でアプリの権限についてご確認下さい。")
      (hu . "Nincs engedély a helyadatokhoz való hozzáféréshez. Ellenőrizze az alkalmazás engedélyeit a rendszerbeállításokban.")
      (fr . "Aucune autorisation d’accès à la localisation. Veuillez vérifier les autorisations de l’application dans les paramètres système.")))
    ("no_permission_to_access_location_once"
     ((en . "No permission to access the location service.")
      (ja . "位置情報へのアクセスの権限がありません。")
      (hu . "Nincs engedély a helyadatokhoz való hozzáféréshez.")
      (fr . "Aucune autorisation d’accès à la localisation.")))
    ("clock_base_panel"
     ((en . "Clock base panel")
      (ja . "時計文字盤パネル")
      (hu . "Óralap panel")
      (fr . "Cadran de l’horloge")))
    ("sky_panel"
     ((en . "Sky panel")
      (ja . "星空パネル")
      (hu . "Égbolt panel")
      (fr . "Voûte céleste")))
    ("sun_and_moon_panel"
     ((en . "Sun and Moon panel")
      (ja . "太陽・月パネル")
      (hu . "Nap és Hold panel")
      (fr . "Soleil et Lune")))
    ("horizon_panel"
     ((en . "Horizon panel")
      (ja . "地平線パネル")
      (hu . "Horizont panel")
      (fr . "Horizon")))
    ("clock_hands_panel"
     ((en . "Clock hands panel")
      (ja . "時計針パネル")
      (hu . "Óramutatók panel")
      (fr . "Aiguilles de l’horloge")))
    ("bg_color"
     ((en . "Background color")
      (ja . "背景色")
      (hu . "Háttérszín")
      (fr . "Couleur d’arrière-plan")))
    ("basic_colors"
     ((en . "Basic Colors")
      (ja . "基本色")
      (hu . "Alapszínek")
      (fr . "Couleurs de base")))
    ("white"
     ((en . "White")
      (ja . "白")
      (hu . "Fehér")
      (fr . "Blanc")))
    ("silver"
     ((en . "Silver")
      (ja . "銀色")
      (hu . "Ezüst")
      (fr . "Argent")))
    ("gray"
     ((en . "Gray")
      (ja . "灰色")
      (hu . "Szürke")
      (fr . "Gris")))
    ("black"
     ((en . "Black")
      (ja . "黒")
      (hu . "Fekete")
      (fr . "Noir")))
    ("red"
     ((en . "Red")
      (ja . "赤")
      (hu . "Piros")
      (fr . "Rouge")))
    ("maroon"
     ((en . "Maroon")
      (ja . "マルーン")
      (hu . "Bordó")
      (fr . "Marron")))
    ("yellow"
     ((en . "Yellow")
      (ja . "黄色")
      (hu . "Sárga")
      (fr . "Jaune")))
    ("olive"
     ((en . "Olive")
      (ja . "オリーブ")
      (hu . "Olívazöld")
      (fr . "Olive")))
    ("lime"
     ((en . "Lime")
      (ja . "ライム")
      (hu . "Limezöld")
      (fr . "Citron vert")))
    ("green"
     ((en . "Green")
      (ja . "緑")
      (hu . "Zöld")
      (fr . "Vert")))
    ("aqua"
     ((en . "Aqua")
      (ja . "水色")
      (hu . "Akvamarin")
      (fr . "Aqua")))
    ("teal"
     ((en . "Teal")
      (ja . "ティール")
      (hu . "Türkiz")
      (fr . "Sarcelle")))
    ("blue"
     ((en . "Blue")
      (ja . "青")
      (hu . "Kék")
      (fr . "Bleu")))
    ("navy"
     ((en . "Navy")
      (ja . "ネイビー")
      (hu . "Sötétkék")
      (fr . "Bleu marine")))
    ("fuchsia"
     ((en . "Fuchsia")
      (ja . "フクシャ")
      (hu . "Fukszia")
      (fr . "Fuchsia")))
    ("purple"
     ((en . "Purple")
      (ja . "紫")
      (hu . "Lila")
      (fr . "Violet")))
    ("hsv_h"
     ((en . "Hue")
      (ja . "色相")
      (hu . "Árny")
      (fr . "Teinte")))
    ("hsv_s"
     ((en . "Sat")
      (ja . "彩度 ")
      (hu . "Tel")
      (fr . "Sat")))
    ("hsv_v"
     ((en . "Val")
      (ja . "明度")
      (hu . "Vil")
      (fr . "Val")))
    ("layoutSettings"
     ((en . "Layout Settings")
      (ja . "レイアウト設定")
      (hu . "Elrendezés beállításai")
      (fr . "Paramètres de mise en page")))
    ("digitalClockPosition"
     ((en . "Digital Clock Position")
      (ja . "デジタル時計の位置")
      (hu . "Digitális óra pozíciója")
      (fr . "Position de l’horloge numérique")))
    ("landscape"
     ((en . "Landscape")
      (ja . "横向き")
      (hu . "Fekvő")
      (fr . "Paysage")))
    ("portrait"
     ((en . "Portrait")
      (ja . "縦向き")
      (hu . "Álló")
      (fr . "Portrait")))
    ("left"
     ((en . "Left")
      (ja . "左")
      (hu . "Bal")
      (fr . "Gauche")))
    ("right"
     ((en . "Right")
      (ja . "右")
      (hu . "Jobb")
      (fr . "Droite")))
    ("top"
     ((en . "Top")
      (ja . "上")
      (hu . "Fent")
      (fr . "Haut")))
    ("bottom"
     ((en . "Bottom")
      (ja . "下")
      (hu . "Lent")
      (fr . "Bas")))))

(define (whole-xml-create lst lang)
  `("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    "<resources xmlns:tools=\"http://schemas.android.com/tools\">"
    ,@(filter
       string?
       (map (lambda (x)
              (let ([name (first x)]
                    [val (get-value (second x) lang)])
                (and val
                     (if (list? val)
                         (and (eq? lang 'en)
                              (string-append
                               "    <string name=\""
                               name
                               "\" translatable=\"false\">"
                               (second val)
                               "</string>"))
                         (string-append
                          "    <string name=\""
                          name
                          "\">"
                          val
                          "</string>")))))
            lst))
    "</resources>"))

(define (get-value vals lang)
  (let ([val (assq lang vals)])
    (or (and val
             (normalize (cdr val)))
        (let ([val (assq '* vals)])
          (and val
               `(#t ,(normalize (cdr val))))))))

(define (normalize text)
  ;; (string-replace text "\"" "\\\""))
  text)

(map (lambda (x)
       (let ([lang (first x)]
             [path (second x)])
         (call-with-output-file (string->path path)
           (lambda (out)
             (for-each (lambda (ln) (displayln ln out))
                       (whole-xml-create value-list lang)))
           #:exists 'replace)))
     lang-list)

(displayln "Completed!")

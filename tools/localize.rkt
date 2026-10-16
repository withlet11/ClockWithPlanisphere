#lang racket

(define lang-list '((en "../app/src/main/res/values/strings.xml")
                    (ja-rJP "../app/src/main/res/values-ja-rJP/strings.xml")
                    (hu-rHU "../app/src/main/res/values-hu-rHU/strings.xml")
                    (fr-rFR "../app/src/main/res/values-fr-rFR/strings.xml")
                    (zh-rCN "../app/src/main/res/values-zh-rCN/strings.xml")))

(define value-list
  '(("app_name"
     ((* . "Clock with Planisphere")))
    ("locationSettings"
     ((en . "Location Settings")
      (ja-rJP . "観測地設定")
      (hu-rHU . "Megfigyelési hely beállítása")
      (fr-rFR . "Paramètres du site d’observation")
      (zh-rCN . "位置设置")))
    ("license"
     ((en . "License")
      (ja-rJP . "使用許諾")
      (hu-rHU . "Licenc")
      (fr-rFR . "Licence")
      (zh-rCN . "许可证")))
    ("opensource_licenses"
     ((en . "OpenSource Licenses")
      (ja-rJP . "オープンソースライセンス")
      (hu-rHU . "Nyílt forráskódú licencek")
      (fr-rFR . "Licences open source")
      (zh-rCN . "开源许可证")))
    ("latitude"
     ((en . "Latitude: ")
      (ja-rJP . "緯度：")
      (hu-rHU . "Szélesség: ")
      (fr-rFR . "Latitude : ")
      (zh-rCN . "纬度：")))
    ("latitudeHint"
     ((* . "+23.4567")))
    ("longitude"
     ((en . "Longitude: ")
      (ja-rJP . "経度：")
      (hu-rHU . "Hosszúság: ")
      (fr-rFR . "Longitude : ")
      (zh-rCN . "经度：")))
    ("longitudeHint"
     ((* . "+123.456")))
    ("gps"
     ((* . "GPS")))
    ("modify"
     ((en . "Modify")
      (ja-rJP . "変更する")
      (hu-rHU . "Módosítás")
      (fr-rFR . "Modifier")
      (zh-rCN . "修改")))
    ("cancel"
     ((en . "Cancel")
      (ja-rJP . "変更しない")
      (hu-rHU . "Mégse")
      (fr-rFR . "Annuler")
      (zh-rCN . "取消")))
    ("inGettingLocation"
     ((en . "in getting location…")
      (ja-rJP . "位置情報を取得中…")
      (hu-rHU . "Helyadatok lekérdezése…")
      (fr-rFR . "Obtention de la localisation…")
      (zh-rCN . "正在获取位置…")))
    ("pleaseCheckIfGPSIsOn"
     ((en . "Please check if GPS is on.")
      (ja-rJP . "GPSがONかチェックして下さい。")
      (hu-rHU . "Ellenőrizze, hogy be van-e kapcsolva a GPS.")
      (fr-rFR . "Veuillez vérifier que le GPS est activé.")
      (zh-rCN . "请检查 GPS 是否已开启。")))
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
      (ja-rJP . "プライバシポリシー")
      (hu-rHU . "Adatvédelmi irányelvek")
      (fr-rFR . "Politique de confidentialité")
      (zh-rCN . "隐私政策")))
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
      (ja-rJP . "<u>最終更新：2020-11-20</u>\\n\\n
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
      (hu-rHU . "<i>Utoljára frissítve: 2020. november 20.</i>\\n\\n
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
      (fr-rFR . "<i>Dernière mise à jour : 20 novembre 2020</i>\\n\\n
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
        <a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>")
      (zh-rCN . "<u>最后更新：2020年11月20日</u>\\n\\n
        本隐私政策旨在说明我们如何处理用户提供的信息以及如何使用这些信息。\\n\\n
        <b>用户数据收集</b>\\n
        我们不会收集用户的个人信息，包括姓名、电话号码、电子邮件地址和密码。但是，本应用使用第三方广告服务（Google AdMob™），该服务可能会收集您的信息。有关该服务的隐私政策，请访问以下页面：
            <a href=\"https://policies.google.com/privacy\">Google 隐私政策</a>\\n\\n
        <b>其他权限</b>\\n
        只有在您授权的情况下，本应用才会访问您的位置信息。这些数据仅存储在您的设备上，不会提供给其他应用或服务。\\n
        本应用使用互联网连接显示广告横幅。\\n\\n
        <b>变更</b>\\n
        本隐私政策可能会因任何原因不时更新。\\n\\n
        <b>您的同意</b>\\n
        使用本应用即表示您同意本隐私政策以及上述第三方的隐私政策。如果您不同意本隐私政策或第三方的隐私政策，可以卸载本应用。\\n\\n
        <b>联系我们</b>\\n
        如果您对本隐私政策有任何疑问，请通过电子邮件联系我们：
        <a href=\"mailto:withlet11@gmail.com\">withlet11@gmail.com</a>")))
    ("north_label"
     ((* . "N")))
    ("south_label"
     ((* . "S")))
    ("ns_switch"
     ((* . "NS Switch")))
    ("no_permission_to_access_location_permanent"
     ((en . "No permission to access the location service. Please check the application permission in the system settings.")
      (ja-rJP . "位置情報へのアクセスの権限がありません。設定でアプリの権限についてご確認下さい。")
      (hu-rHU . "Nincs engedély a helyadatokhoz való hozzáféréshez. Ellenőrizze az alkalmazás engedélyeit a rendszerbeállításokban.")
      (fr-rFR . "Aucune autorisation d’accès à la localisation. Veuillez vérifier les autorisations de l’application dans les paramètres système.")
      (zh-rCN . "没有访问定位服务的权限。请在系统设置中检查本应用的权限。")))
    ("no_permission_to_access_location_once"
     ((en . "No permission to access the location service.")
      (ja-rJP . "位置情報へのアクセスの権限がありません。")
      (hu-rHU . "Nincs engedély a helyadatokhoz való hozzáféréshez.")
      (fr-rFR . "Aucune autorisation d’accès à la localisation.")
      (zh-rCN . "没有访问定位服务的权限。")))
    ("clock_base_panel"
     ((en . "Clock base panel")
      (ja-rJP . "時計文字盤パネル")
      (hu-rHU . "Óralap panel")
      (fr-rFR . "Cadran de l’horloge")
      (zh-rCN . "钟面面板")))
    ("sky_panel"
     ((en . "Sky panel")
      (ja-rJP . "星空パネル")
      (hu-rHU . "Égbolt panel")
      (fr-rFR . "Voûte céleste")
      (zh-rCN . "星空面板")))
    ("sun_and_moon_panel"
     ((en . "Sun and Moon panel")
      (ja-rJP . "太陽・月パネル")
      (hu-rHU . "Nap és Hold panel")
      (fr-rFR . "Soleil et Lune")
      (zh-rCN . "太阳和月亮面板")))
    ("horizon_panel"
     ((en . "Horizon panel")
      (ja-rJP . "地平線パネル")
      (hu-rHU . "Horizont panel")
      (fr-rFR . "Horizon")
      (zh-rCN . "地平线面板")))
    ("clock_hands_panel"
     ((en . "Clock hands panel")
      (ja-rJP . "時計針パネル")
      (hu-rHU . "Óramutatók panel")
      (fr-rFR . "Aiguilles de l’horloge")
      (zh-rCN . "时针面板")))
    ("bg_color"
     ((en . "Background color")
      (ja-rJP . "背景色")
      (hu-rHU . "Háttérszín")
      (fr-rFR . "Couleur d’arrière-plan")
      (zh-rCN . "背景颜色")))
    ("basic_colors"
     ((en . "Basic Colors")
      (ja-rJP . "基本色")
      (hu-rHU . "Alapszínek")
      (fr-rFR . "Couleurs de base")
      (zh-rCN . "基本颜色")))
    ("white"
     ((en . "White")
      (ja-rJP . "白")
      (hu-rHU . "Fehér")
      (fr-rFR . "Blanc")
      (zh-rCN . "白色")))
    ("silver"
     ((en . "Silver")
      (ja-rJP . "銀色")
      (hu-rHU . "Ezüst")
      (fr-rFR . "Argent")
      (zh-rCN . "银色")))
    ("gray"
     ((en . "Gray")
      (ja-rJP . "灰色")
      (hu-rHU . "Szürke")
      (fr-rFR . "Gris")
      (zh-rCN . "灰色")))
    ("black"
     ((en . "Black")
      (ja-rJP . "黒")
      (hu-rHU . "Fekete")
      (fr-rFR . "Noir")
      (zh-rCN . "黑色")))
    ("red"
     ((en . "Red")
      (ja-rJP . "赤")
      (hu-rHU . "Piros")
      (fr-rFR . "Rouge")
      (zh-rCN . "红色")))
    ("maroon"
     ((en . "Maroon")
      (ja-rJP . "マルーン")
      (hu-rHU . "Bordó")
      (fr-rFR . "Marron")
      (zh-rCN . "栗色")))
    ("yellow"
     ((en . "Yellow")
      (ja-rJP . "黄色")
      (hu-rHU . "Sárga")
      (fr-rFR . "Jaune")
      (zh-rCN . "黄色")))
    ("olive"
     ((en . "Olive")
      (ja-rJP . "オリーブ")
      (hu-rHU . "Olívazöld")
      (fr-rFR . "Olive")
      (zh-rCN . "橄榄绿")))
    ("lime"
     ((en . "Lime")
      (ja-rJP . "ライム")
      (hu-rHU . "Limezöld")
      (fr-rFR . "Citron vert")
      (zh-rCN . "亮绿色")))
    ("green"
     ((en . "Green")
      (ja-rJP . "緑")
      (hu-rHU . "Zöld")
      (fr-rFR . "Vert")
      (zh-rCN . "绿色")))
    ("aqua"
     ((en . "Aqua")
      (ja-rJP . "水色")
      (hu-rHU . "Akvamarin")
      (fr-rFR . "Aqua")
      (zh-rCN . "水蓝色")))
    ("teal"
     ((en . "Teal")
      (ja-rJP . "ティール")
      (hu-rHU . "Türkiz")
      (fr-rFR . "Sarcelle")
      (zh-rCN . "蓝绿色")))
    ("blue"
     ((en . "Blue")
      (ja-rJP . "青")
      (hu-rHU . "Kék")
      (fr-rFR . "Bleu")
      (zh-rCN . "蓝色")))
    ("navy"
     ((en . "Navy")
      (ja-rJP . "ネイビー")
      (hu-rHU . "Sötétkék")
      (fr-rFR . "Bleu marine")
      (zh-rCN . "海军蓝")))
    ("fuchsia"
     ((en . "Fuchsia")
      (ja-rJP . "フクシャ")
      (hu-rHU . "Fukszia")
      (fr-rFR . "Fuchsia")
      (zh-rCN . "紫红色")))
    ("purple"
     ((en . "Purple")
      (ja-rJP . "紫")
      (hu-rHU . "Lila")
      (fr-rFR . "Violet")
      (zh-rCN . "紫色")))
    ("hsv_h"
     ((en . "Hue")
      (ja-rJP . "色相")
      (hu-rHU . "Árnyalat")
      (fr-rFR . "Teinte")
      (zh-rCN . "色相")))
    ("hsv_s"
     ((en . "Sat")
      (ja-rJP . "彩度 ")
      (hu-rHU . "Tel")
      (fr-rFR . "Sat")
      (zh-rCN . "饱和度")))
    ("hsv_v"
     ((en . "Val")
      (ja-rJP . "明度")
      (hu-rHU . "Vil")
      (fr-rFR . "Val")
      (zh-rCN . "明度")))
    ("layoutSettings"
     ((en . "Layout Settings")
      (ja-rJP . "レイアウト設定")
      (hu-rHU . "Elrendezés beállításai")
      (fr-rFR . "Paramètres de mise en page")
      (zh-rCN . "布局设置")))
    ("digitalClockPosition"
     ((en . "Digital Clock Position")
      (ja-rJP . "デジタル時計の位置")
      (hu-rHU . "Digitális óra pozíciója")
      (fr-rFR . "Position de l’horloge numérique")
      (zh-rCN . "数字时钟位置")))
    ("landscape"
     ((en . "Landscape")
      (ja-rJP . "横向き")
      (hu-rHU . "Fekvő")
      (fr-rFR . "Paysage")
      (zh-rCN . "横向")))
    ("portrait"
     ((en . "Portrait")
      (ja-rJP . "縦向き")
      (hu-rHU . "Álló")
      (fr-rFR . "Portrait")
      (zh-rCN . "纵向")))
    ("left"
     ((en . "Left")
      (ja-rJP . "左")
      (hu-rHU . "Bal")
      (fr-rFR . "Gauche")
      (zh-rCN . "左")))
    ("right"
     ((en . "Right")
      (ja-rJP . "右")
      (hu-rHU . "Jobb")
      (fr-rFR . "Droite")
      (zh-rCN . "右")))
    ("top"
     ((en . "Top")
      (ja-rJP . "上")
      (hu-rHU . "Fent")
      (fr-rFR . "Haut")
      (zh-rCN . "上")))
    ("bottom"
     ((en . "Bottom")
      (ja-rJP . "下")
      (hu-rHU . "Lent")
      (fr-rFR . "Bas")
      (zh-rCN . "下")))))



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

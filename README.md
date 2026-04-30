```markdown
# RSA skaitmeninio parašo įgyvendinimas

Šis projektas yra skirtas **mokymosi tikslams** ir demonstruoja, kaip praktiškai realizuoti
RSA pagrindu veikiantį skaitmeninį parašą, jo tikrinimą ir galimą atakos scenarijų.

Projektą sudaro trys atskiros programos:

- `rsa-signer-client` – klientas, kuris suformuoja pranešimą ir pasirašo jį RSA privačiu raktu.
- `rsa-verifier-server` – serveris, kuris gauna pranešimą su parašu ir patikrina parašo teisingumą viešuoju raktu.
- `rsa-proxy-attacker` – tarpinis komponentas (simuliuojamas užpuolikas), skirtas pademonstruoti, kas nutinka pažeidus duomenų vientisumą ar perdavimo patikimumą.

---

## 1. Projekto tikslas

Pagrindinis tikslas – suprasti skaitmeninio parašo principą:

1. Kaip pranešimas yra pasirašomas privačiu raktu.
2. Kaip parašas tikrinamas viešuoju raktu.
3. Kaip aptinkami pakeitimai pranešime ar paraše.
4. Kodėl svarbus saugus raktų valdymas ir saugus perdavimo kanalas.

---

## 2. Naudojamos sąvokos

- **RSA** – asimetrinė kriptografija, naudojanti raktų porą: privatų ir viešą.
- **Skaitmeninis parašas** – kriptografinis patvirtinimas, kad pranešimą pasirašė rakto savininkas.
- **Hash (santrauka)** – fiksuoto ilgio pranešimo atspaudas, naudojamas pasirašymui/tikrinimui.
- **Vientisumas** – garantija, kad duomenys nebuvo pakeisti.
- **Autentiškumas** – patvirtinimas, kad siuntėjas yra tas, kuo prisistato.

---

## 3. Programų paskirtis ir atsakomybės

### `rsa-signer-client`

Ši programa:

- sukuria arba priima pranešimo tekstą;
- apskaičiuoja pranešimo santrauką (hash);
- pasirašo santrauką RSA privačiu raktu;
- išsiunčia pranešimą ir parašą tikrinimo pusei.

Rezultatas: gauname porą **(pranešimas + parašas)**.

### `rsa-verifier-server`

Ši programa:

- priima pasirašytą pranešimą;
- apskaičiuoja gauto pranešimo santrauką;
- RSA viešuoju raktu patikrina, ar parašas atitinka pranešimą;
- pateikia aiškų atsakymą: parašas **galiojantis** arba **negaliojantis**.

Rezultatas: nustatoma, ar duomenys autentiški ir nepakeisti.

### `rsa-proxy-attacker`

Ši programa naudojama demonstraciniams testams:

- gali perimti siunčiamą srautą;
- gali mėginti pakeisti pranešimą ar parašą;
- leidžia stebėti, kaip tikrinimo sistema reaguoja į suklastotus duomenis.

Rezultatas: praktiškai parodoma, kad pakeisti duomenys neatitinka parašo ir yra atmetami.

---

## 4. Aukšto lygio veikimo schema

1. `rsa-signer-client` pasirašo pranešimą.
2. Pranešimas su parašu siunčiamas į `rsa-verifier-server` (tiesiogiai arba per `rsa-proxy-attacker`).
3. `rsa-verifier-server` atlieka parašo patikrą.
4. Grąžinamas patikros rezultatas.

---

## 5. Reikalavimai aplinkai

Kadangi projekte naudojamas `pom.xml`, daroma prielaida, kad programos yra Java/Maven pagrindu.

Rekomenduojama turėti:

- **JDK 17+** (arba pagal dėstytojo nurodytą versiją),
- **Maven 3.8+**,
- terminalą / IDE (pvz., IntelliJ IDEA).

> Pastaba: jei laboratoriniame darbe nurodyta kita Java versija, vadovaukitės užduotimi.

---

## 6. Kaip paleisti projektą (bendras principas)

Paleidimo seka dažniausiai tokia:

1. Paleisti `rsa-verifier-server`.
2. (Pasirinktinai) paleisti `rsa-proxy-attacker`, jei norima atakos scenarijaus.
3. Paleisti `rsa-signer-client` ir išsiųsti pasirašytą pranešimą.

Jei moduliai yra atskiri Maven projektai, kiekviename kataloge galima vykdyti:

```bash
mvn clean package
mvn spring-boot:run
```

arba paleisti pagrindinę klasę iš IDE.

> Jei projektas nėra Spring Boot tipo, vietoje `spring-boot:run` naudokite paleidimą per `java -jar ...` arba per IDE `Main` klasę.

---

## 7. Siūlomi testavimo scenarijai studentui

### Scenarijus A – Teisingas parašas

- Klientas pasirašo originalų pranešimą.
- Serveris patvirtina parašą.
- Tikimasi rezultato: **galiojantis parašas**.

### Scenarijus B – Pakeistas pranešimas

- Po pasirašymo pakeičiamas bent vienas simbolis pranešime.
- Serveris atmeta parašą.
- Tikimasi rezultato: **negaliojantis parašas**.

### Scenarijus C – Pakeistas parašas

- Pranešimas paliekamas tas pats, bet sugadinamas parašas.
- Serveris atmeta parašą.
- Tikimasi rezultato: **negaliojantis parašas**.

### Scenarijus D – Atakos demonstracija per proxy

- Duomenys perduodami per `rsa-proxy-attacker`.
- Proxy bando pakeisti srautą.
- Serveris aptinka neatitikimą.
- Tikimasi rezultato: **saugumo pažeidimas aptiktas**.

---

## 8. Dažniausios problemos ir sprendimai

- **Nesutampa raktų pora**  
  Patikrinkite, ar serveris tikrina tuo viešuoju raktu, kuris atitinka kliento privatų raktą.

- **Blogas duomenų formatas tarp modulių**  
  Įsitikinkite, kad pranešimo koduotė (`UTF-8`) ir parašo serializavimo formatas vienodas.

- **Nepasiekiamas serveris / neteisingas portas**  
  Patikrinkite `host` ir `port` konfigūraciją.

- **Skirtingos Java/Maven versijos**  
  Suderinkite įrankių versijas pagal užduoties reikalavimus.

---

## 9. Saugumo pastabos

Šis projektas skirtas mokymuisi, todėl:

- nelaikykite privačių raktų viešai pasiekiamuose kataloguose;
- nenaudokite demonstracinių raktų realiose sistemose;
- nenaudokite šio kodo tiesiogiai produkcinėje aplinkoje be papildomų saugumo priemonių.

---

## 10. Išvada

Projektas padeda praktiškai suprasti RSA skaitmeninio parašo mechanizmą:
nuo parašo kūrimo iki verifikavimo bei atakų aptikimo.
Tai bazinis, bet labai svarbus žingsnis mokantis taikomosios kriptografijos ir saugių sistemų kūrimo.
```
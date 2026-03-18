# Kasutajalood (Discord chatbot, mis aitab arendajal vastata juhi küsimustele)

Kasutajalood on pandud kirja **arendaja vaatenurgast**, sest bot aitab arendajal juhile vastata.

---

## 1

**Kasutajana** (tarkvaraarendajana)
**tahan**, et Discord chatbot genereeriks vastuse juhilt tulnud tehnilistele küsimustele,
**et** juht saaks kiiresti vastused ja ma ei peaks oma arendustööd katkestama.

**Näidisküsimused**

* Kust ma leian Amadeus API dokumentatsiooni?
* Mis tüüpi koordinaate ArcGIS toetab?

---

## 2

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot selgitaks tehnilisi teemasid lihtsalt ja mõistetavalt,
**et** mitte IT-taustaga juht neist paremini aru saaks.

**Näidisküsimused**

* Milleks projektis GeoJSONit kasutatakse?
* Mis vahe on WGS84 ja Web Mercator vahel (ArcGIS)?

---

## 3

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot võrdleks erinevaid arhitektuurilahendusi,
**et** aidata juhil neid kliendile paremini selgitada.

**Näidisküsimused**

* Kas X infosüsteemi saaks näidata kliendi infosüsteemis ilma autentimata (sisevõrgus, VPN või IP põhise lahendusena)?
* Kas kliendil oleks lahenduse loomiseks parem kasutada mikroteenuseid või monoliiti?

---

## 4

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot aitaks anda esialgseid arendusmahu hinnanguid,
**et** vastata projektijuhi küsimustele.

**Näidisküsimused**

* Kui keeruline oleks teha trepikonfiguraator 12 tootega?
* Kui kaua võtab React/Three.js-is aega üheksa 3D tooteperekonna PDF-ide genereerimine koos mõõtjoonte lisamisega?

---

## 5

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot koostaks lühikese tehnilise selgituse,
**et** säästa aega vastuse kirjutamisel.

**Näidisküsimused**

* Mis on süsteemi arhitektuuri üldnõuded?
* Mis plussid-miinused on X arhitektuuri rakendmisel nende teenuste disainimiseks?

---

## 6

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot selgitaks erinevaid autentimislahendusi,
**et** aidata juhil vastata kliendile arhitektuurivalikute osas.

**Näidisküsimused**

* Kas API key autentimine sobiks antud infosüsteemi jaoks?
* kui keeruline on konkreetsel juhul rakendada OAuth2.0 vs. token-based authentication?

---
## 7

**Kasutajana** (tarkvaraarendajana)
**tahan**, et chatbot teeks lühikokkuvõtte tehnilise lahendusega seotud põhipunktidest,
**et** juhil oleks kliendiga suheldes piisav ülevaade loodavast lahendusest.

**Näidisküsimused**

* Mis on selle konkreetse projekti arhitektuuri põhinõuded?
* Mis on kliendi ootused tehnilise lahenduse osas?
* Millised mittefunktsionaalseid nõuded on lahenduse jaoks olulised (nt jõudlus, turvalisus, skaleeritavus)?

---

# Näidisküsimused teemade kaupa (Discord chatbot äpi jaoks)

Allpool mõned konkreetsed näited, mida **juhid tarkvaraarendajalt küsivad** ning millele chatbot saab aidata vastata

---

## Arhitektuur ja tehnoloogia

1. Mis oleks X veebilehe organisatsioonisiseseks kuvamiseks parim autentimislahendus – avalikuks teha, API key-ga, tokeniga vm?
2. Kas kaardilahendust saaks kasutada ilma autentimata (sisevõrgus, VPN või IP põhise ligipääsuga)?
3. Kas selle jaoks on vaja eraldi backend API EP ja teenust?
4. Kas saab kasutada OAuth2.0 või token põhist autentimist?

---

## Andmebaasid ja süsteemid

5. Kas EHR kasutab PostgreSQL andmebaasi?
6. Kas doEhitus on õige nimi või typo (EHR veebileht)?
7. Kas EHR kasutas Pythoni / Nodejs baasil lahendusi?
8. Mis andmed saab kätte Teeregistri API teede layerist?
9. Kas Teeregistri REST API *Teed* layeris on olemas info kilomeetrite ja teepikkuste kohta?
10. Kas selle projekti jaoks sobiks paremini tavaline Node.js BE, NextJS vm?

---

## Arendusmaht ja hinnangud

11. Kui kiiresti programmeerib üks arendaja valmis treppide konfiguraatori (algusest lõpuni, veebilehel konf olemas)?
12. Kui keeruline oleks teha kliendile customized lahendus (***töö nimetus***) osas?
13. Mitu tundi võiks konkreetne feature nõuda? / Kas selle jõuaks valmis 2 päevaga?
14. Kui keeruline oleks teha trepikonfiguraator 12 tootega?
15. Kui kaua võtab React/Three.js-is aega üheksa 3D tooteperekonna PDF-ide genereerimine koos mõõtjoonte lisamisega?
16. Kui kaua võtaks aega customized lahenduse loomine vastavalt kliendi nõuetele (nimekiri nõuetest antud)?
17. Kui kaua võtaks aega erinõuetele vastava permeetri arvutuste implementeerimine 7 erinevale tooteperekonnale 3D vaadetes?

---

## Kood ja integratsioonid (arendus)

18. Kas lahenduse integratsioon kaardirakendusega on liiga keeruline?
19. Kui hästi see lahendus skaleeritav oleks?
20. Kas saame uue X lahenduse loomisel kasutada olemasolevaid komponente?
21. Kas loodav lahendus / arhitektuur / struktuur on pikaajaliselt jätkusuutlik?

---

## Dokumentatsioon

22. Kus ma leian Amadeus API dokumentatsiooni?
23. Millised mittefunktsionaalsed nõuded on lahenduse jaoks olulised (nt jõudlus, turvalisus, skaleeritavus)?
24. Milised on lahenduse funkstionaalsed nõuded (nt kasutajate rollid, peamised funktsioonid, integratsioonid)?
25. Millised on lahenduse turvanõuded?
26. Millised on konkreetse projekti arhitektuuri põhinõuded?

---

## Üldised küsimused

27. Ma detailidesse ei jõua süveneda, kirjuta kokkuvõte X teemal kui pilt selge.
28. Kuidas (***rakenduse nimi***) prelive jaoks UI deploy tarne ticketeid teha?

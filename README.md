# UrbanScoot

Applicazione desktop Java (Swing) per il noleggio di monopattini elettrici in sharing.
Progetto del corso di Ingegneria del Software (UniCT) – Christian Cultraro, Gaetano Mazzaglia.

## Requisiti

- JDK 17 o superiore (testato con JDK 21)
- La libreria JUnit è già inclusa nella cartella `lib/`

## Struttura del repository

```
src/
  controller/   UrbanScoot (controller di facciata) e gestori
  dominio/      Monopattino, Corsa, Flotta, Cliente, tariffe...
  stati/        stati del Monopattino (pattern State)
  ui/           interfaccia grafica Swing (main in FinestraLogin)
  test/         test JUnit 5
lib/            junit-platform-console-standalone
docs/           documentazione (Elaborato, iterazioni, file Astah, testing)
```

## Compilazione ed esecuzione

Dalla cartella principale del progetto:

**Windows**
```
javac -d bin -cp "lib/*" src/controller/*.java src/dominio/*.java src/stati/*.java src/ui/*.java src/test/*.java
java -cp "bin;lib/*" ui.FinestraLogin
```

**Linux / macOS**
```
javac -d bin -cp "lib/*" $(find src -name "*.java")
java -cp "bin:lib/*" ui.FinestraLogin
```

In alternativa, con VS Code (Extension Pack for Java): aprire la cartella del progetto
e premere **Run** sul metodo `main` di `src/ui/FinestraLogin.java`.

### Credenziali di prova

| Ruolo          | Username | Password |
|----------------|----------|----------|
| Cliente        | utente   | utente   |
| Amministratore | admin    | admin    |

Si può anche registrare un nuovo cliente dalla schermata di login.

## Esecuzione dei test

Dopo aver compilato (comando sopra):

```
java -jar lib/junit-platform-console-standalone-1.14.4.jar execute -cp bin --scan-classpath
```

Risultato atteso: 33 test eseguiti, 33 superati.
Da VS Code si possono lanciare anche dalla scheda **Testing**.

## Documentazione

- `docs/Elaborato_UrbanScoot.pdf` – documento completo (Ideazione, Iterazioni 1–5, Testing)
- `docs/PROGETTAZIONE/` – PDF e file Astah (`.asta`) di ogni iterazione
- `docs/IDEAZIONE/` – documento di visione, glossario, casi d'uso, regole di dominio
- `docs/TESTING/` – documento di testing

<img src="data/image/13016_uni-insubria-wcj.png"  align="center">


<p align="center">
  <img src="data/image/logo.png" width="390" height="595">
</p>

--- 

# 🔪 TheKnife 

--- 

PROGETTO THEKNIFE
LABORATORIO A, CORSO DI LAUREA TRIENNALE IN INFORMATICA
UNIVERSITA' DEGLI STUDI DELL'INSUBRIA

MANUALE UTENTE, MANUALE TECNICO DISPONIBILI IN [doc](doc)

--- 

# 👥 AUTORI

- **[Oittijo Ahemmed Sarkar](https://github.com/ThatsKool)** - 759646 VARESE - Project manager, Design Manager
- **[Federico Barbotti](https://github.com/FedericoBarbotti)** - 752545 VARESE - System architect
- **[Bennajim Ali](https://github.com/alibennajim)** - 760125 VARESE - Document & quality manager

--- 

# 📦 DIPENDENZE PRINCIPALI

- **OpenJFX / JavaFx 21.0.4**
- **Apache Common CSV**
- **Junit Jupiter 5.9.1**
- **Gradle OPENJFX Plugin 0.1.0**
- **Java JDK 17 o superiore**

--- 

# 🖥️ SISTEMA OPERATIVO
L'applicazione è stata sviluppata per windows ma contiene tutte le librerie necessarie per l'esecuzione su altri sistemi operativi quali linux e mac. Tuttavia il corretto funzionamento dell'applicazione è garantito su sistemi windows. 

---

# ⚙️ AVVIO APPLICAZIONE

- Nella directory [bin](bin) scegliere il proprio sistema operativo e doppio click sul file `.jar`
- In alternativa sarà possibile far partire l'applicazione dal prompt dei comandi seguendo queste istruzioni:
```cmd
   cd <percorso in cui è stato estratto l'archivio>
```

```cmd
   gradlew run
```

---

# 📁 SALVATAGGIO DATI

I dati dell'applicazione (utenti, ristoranti, recensioni, preferiti) vengono **salvati nella directory locale dell'utente**, non nel progetto:

- **Windows:** `C:\Users\<tuouser>\.theknife\data`
- **macOS:** `~/Library/Application Support/TheKnife/data`
- **Linux:** `~/.local/share/theknife/data`

Al primo avvio, solo i file CSV vengono copiati dalla cartella `data` del progetto (o da quella accanto al JAR) nella directory utente. Le immagini restano nella cartella `data` del progetto/JAR e non vengono copiate.

---

# 📹 BREVE VIDEO DELL'INTERFACCIA GRAFICA

<p align="center">
  <img src="data/image/video.gif" alt="GIF GUI">
</p>

<p align="center">
  <video src="data/image/video_gui.mp4" controls>
    Il tuo browser non supporta il tag video.
  </video>
</p>

--- 


# TNTZones
Erstellt temporäre (WorldEdit)-Regionen in denen Spieler TNT nutzen können 

![](https://craft-together.de/~irgendsoeintyp/ezgif-7-3a71f69a5c83.gif)

**TODO:**
- Lebensdauer / Größe der TNTZone konfigurierbar machen. (Aktuell hardcoded auf 48x48 / 120 Sekunden)
- Permission: `tntzone.shared` umbenennen in: `tntzone.create.shared`


**Befehle:**

`/tntzone create [shared]` - Erstellt eine TNTZone (shared = optional, jeder kann hier TNT nutzen)

`/tntzone list` - Listet vorhandene TNTZonen auf

`/tntzone remove [id]` - Löscht eine TNTZone

`/tntzone tp [id]` - Teleportiert dich zu einer TNTZone


**Permissions:**


Für Spieler:

`tntzone.create` - Erlaubt das erstellen von TNTZonen grundlegend.

`tntzone.create.multiple` - Erlaubt das erstellen mehrerer TNTZonen gleichzeitig.

`tntzone.shared` - Erlaubt das erstellen von öffentlichen TNTZonen (Jeder kann hier TNT nutzen)


Für Moderatoren/Admins:

`tntzone.remove` - Erlaubt das entfernen fremder TNTZonen (eigene können immer entfernt werden!)

`tntzone.inform` - Spieler mit dieser Permission werden informiert sobald jemand eine TNTZone erstellt hat.

`tntzone.list` - Erlaubt das auflisten aller vorhandenen TNTZonen auf dem Server.

`tntzone.teleport` - Erlaubt das teleportieren zu TNTZonen

# Traffic Simulation

A Java Swing traffic simulation that models:

- Traffic signals with `RED`, `GREEN`, and `YELLOW` states
- Lane-based movement for cars and pedestrians
- Behavior strategies for normal and aggressive agents
- Random accident and congestion events
- A visual simulation panel for live playback

## Project Structure

```text
traffic-simulation/
├── src/
│   └── main/
│       ├── Main.java
│       ├── agents/
│       ├── behavior/
│       ├── engine/
│       ├── environment/
│       ├── events/
│       ├── traffic/
│       └── ui/
├── bin/
└── README.md
```

## Compile

PowerShell:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
$files = Get-ChildItem -Path src/main -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d bin $files
```

## Run

```powershell
java -cp bin Main
```

## Notes

- Cars remain constrained to their assigned lanes.
- Signals alternate the right-of-way at the central intersection.
- Accident events block an entire lane for a limited duration.
- Congestion events create slowdown zones that reduce agent speed.
- The current grid is intentionally compact so the architecture stays easy to extend.

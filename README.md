# Traffic Simulation

A Java Swing traffic simulation that models:

- Traffic signals with `RED`, `GREEN`, and `YELLOW` states
- Lane-based movement for cars and pedestrians
- Pedestrian crossing rules based on conflicting vehicle signals
- Behavior strategies for normal and aggressive agents
- Random accident and congestion events
- A live UI with road labels, stop markers, signal summaries, and event status

## Project Structure

```text
traffic-simulation/
|-- src/
|   `-- main/
|       |-- Main.java
|       |-- agents/
|       |-- behavior/
|       |-- engine/
|       |-- environment/
|       |-- events/
|       |-- traffic/
|       `-- ui/
|-- bin/
|-- CODE_WALKTHROUGH.md
`-- README.md
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
- Pedestrians stay on the crosswalk lanes and only proceed when the conflicting vehicle direction is red.
- Accident events block an entire lane for a limited duration.
- Congestion events create slowdown zones that reduce agent speed.
- The UI shows road names, direction hints, colored stop markers, signal summaries, and active events so the simulation is easier to read.
- The current grid is intentionally compact so the architecture stays easy to extend.

## Current UI Guide

- `Main Street` is the horizontal road for cars.
- `Central Avenue` is the vertical road for cars.
- `Crosswalk` is the pedestrian path through the junction.
- Colored stop markers near the junction show which vehicle approach must stop or may proceed.
- The top-left card summarizes which road currently has the right-of-way.
- The right-side dashboard shows metrics, signal status, legend items, and active events.

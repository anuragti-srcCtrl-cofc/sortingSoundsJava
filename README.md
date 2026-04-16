# 🎵 Sorting Sounds — A Sorting Algorithm Visualizer with Sound

**By Anurag Tiwari — College of Charleston, 2025**

A Java Swing application that visualizes sorting algorithms in real time and plays musical tones mapped to bar heights using a **pentatonic scale**. Watch sorting happen step by step while hearing the data transform from chaos into order.

> Inspired by [**"The Sound of Sorting"**](http://panthema.net/2013/sound-of-sorting/) by **Timo Bingmann** (panthema.net) — a popular C++/SDL program that visualizes and audibilizes sorting algorithms, famously featured in the viral YouTube video *"15 Sorting Algorithms in 6 Minutes."*

---

## 🎬 Features

- **6 Sorting Algorithms:** Bubble Sort, Selection Sort, Insertion Sort, Merge Sort, Quick Sort, and Heap Sort
- **Real-Time Visualization:** Bars represent array values, highlighted in magenta during comparisons/swaps
- **Musical Sound Feedback:** Each bar plays a tone from the C pentatonic scale (C-D-E-G-A) across 3 octaves
- **Step Controls:** Play/Pause, Next Step, Next Epoch, and Reset
- **Sound Toggle:** Mute/unmute button for silent viewing
- **Dark Theme:** Clean dark UI with cyan bars and magenta highlights

---

## 🔊 How the Sound Works

Instead of using arbitrary frequencies, each bar height is mapped to a note on the **C major pentatonic scale** — a 5-note scale (C, D, E, G, A) that always sounds harmonious no matter which notes play together.

### The Scale (3 Octaves)

| Octave | Notes (Hz) |
|--------|-----------|
| 4 | C4 (261.6), D4 (293.7), E4 (329.6), G4 (392.0), A4 (440.0) |
| 5 | C5 (523.3), D5 (587.3), E5 (659.3), G5 (784.0), A5 (880.0) |
| 6 | C6 (1046.5), D6 (1174.7), E6 (1318.5), G6 (1568.0), A6 (1760.0) |

### Sound Generation

1. **Bar height → Note index:** Each bar value (50–349) maps linearly to one of 15 pentatonic notes
2. **Sine wave synthesis:** A pure sine wave is generated at the note's frequency
3. **Hann window envelope:** Each tone (~30ms) fades in and out smoothly using a Hann window function, eliminating clicks and pops
4. **Pre-computed clips:** All 15 tones are generated once at startup as `javax.sound.sampled.Clip` objects — no streaming, no buffer underruns
5. **16-bit audio at 44.1kHz:** CD-quality sample rate for clean sound

Short bars → low notes (C4, ~262 Hz). Tall bars → high notes (A6, ~1760 Hz). As the array sorts, you hear the pitch sweep upward — chaos becomes melody.

---

## 🚀 How to Run

### Prerequisites
- **Java 17+** (uses switch expressions and other modern Java features)

### Compile and Run
```bash
# Clone the repository
git clone https://github.com/anuragti-srcCtrl-cofc/sortingSoundsJava.git
cd sortingSoundsJava

# Compile
javac -d bin src/SortingVisualizer.java

# Run
java -cp bin SortingVisualizer
```

### Or in Eclipse
1. Import as existing Java project
2. Run `SortingVisualizer.java`

---

## 🎮 Controls

| Button | Action |
|--------|--------|
| **Algorithm Dropdown** | Select sorting algorithm |
| **Play / Pause** | Start/stop auto-stepping |
| **Next Step** | Advance one comparison/swap |
| **Next Epoch** | Skip to next pass/partition |
| **Reset** | Randomize the array |
| **Sound Off / On** | Toggle sound |

---

## 🏗️ Architecture

The project uses an **OOP Strategy Pattern** for sorting algorithms:

- **`SortingVisualizer`** — Main JFrame with controls and timer
- **`VisualizerPanel`** — JPanel that draws bars and coordinates stepping/sound
- **`SoundPlayer`** — Pre-computed pentatonic Clip system
- **`Sorter`** (abstract) — Base class with `step()`, `isHighlighted()`, `getHighlightedIndex()`
  - `BubbleSorter`, `SelectionSorter`, `InsertionSorter`, `MergeSorter`, `QuickSorter`, `HeapSorter`

Each sorter implements one-step-at-a-time iteration so the visualizer can animate every comparison.

---

## 🙏 Credits & Acknowledgments

- **Anurag Tiwari** — Author, College of Charleston 2025
- **Timo Bingmann** ([panthema.net](http://panthema.net)) — Creator of [*"The Sound of Sorting"*](http://panthema.net/2013/sound-of-sorting/), the direct inspiration for this project. His 2013 C++ program, built for an undergraduate algorithms course at KIT Karlsruhe, became one of the most well-known modern implementations of sorting audibilization.

### Historical Context

The concept of audibilizing (or sonifying) sorting algorithms predates Bingmann's work:

- **Microsoft QBasic SORTDEMO.BAS (1991)** — Microsoft included an audibilized sorting demo in QBasic over two decades before modern web versions
- **andrut (YouTube)** — Released earlier sorting audibilization videos that served as a primary guideline for Bingmann's sound design
- **Digiano & Baecker (1990s)** — Academic researchers who explored "auralization" — making software behavior audible — in the early 1990s

Bingmann's contribution was creating a polished, open-source, educational tool that allowed students to slow down and step through algorithms, addressing limitations he found in existing YouTube demos.

---

## 📄 License

This project is for educational purposes.

---

*This README was generated with the assistance of Google Gemini Flash.*

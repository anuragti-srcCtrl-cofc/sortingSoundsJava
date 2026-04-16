import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.Timer;

public class SortingVisualizer extends JFrame {
	private final VisualizerPanel panel;
	private final JComboBox<String> selector;
	private final JButton playBtn;
	private Timer timer;
	private boolean isPlaying = false;

	public SortingVisualizer() {
		setTitle("OOP Sorting Visualizer with Sound");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		panel = new VisualizerPanel(80);
		add(panel, BorderLayout.CENTER);

		// credtis bar at top
		JPanel creditsPanel = new JPanel(new BorderLayout());
		creditsPanel.setBackground(new Color(30, 30, 30));
		creditsPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		JLabel authorLabel = new JLabel("\u00A9 Anurag Tiwari - College of Charleston 2025");
		authorLabel.setForeground(new Color(180, 180, 180));
		authorLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		JLabel inspirationLabel = new JLabel("Inspired by \"The Sound of Sorting\" by Timo Bingmann (panthema.net)");
		inspirationLabel.setForeground(new Color(130, 130, 130));
		inspirationLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		creditsPanel.add(authorLabel, BorderLayout.WEST);
		creditsPanel.add(inspirationLabel, BorderLayout.EAST);
		add(creditsPanel, BorderLayout.NORTH);

		// control panel with all the butttons
		JPanel controls = new JPanel();
		selector = new JComboBox<>(new String[] { "Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort",
				"Quick Sort", "Heap Sort" });
		playBtn = new JButton("Play");
		JButton stepBtn = new JButton("Next Step");
		JButton epochBtn = new JButton("Next Epoch");
		JButton resetBtn = new JButton("Reset");
		JButton soundBtn = new JButton("Sound Off");

		// hook up button listneres
		playBtn.addActionListener(e -> togglePlay());
		stepBtn.addActionListener(e -> {
			stop();
			panel.step();
		});
		epochBtn.addActionListener(e -> {
			stop();
			panel.nextEpoch();
		});
		resetBtn.addActionListener(e -> {
			stop();
			panel.reset();
		});
		selector.addActionListener(e -> {
			stop();
			panel.setAlgorithm((String) selector.getSelectedItem());
		});
		soundBtn.addActionListener(e -> {
			boolean muted = panel.toggleMute();
			soundBtn.setText(muted ? "Sound On" : "Sound Off");
		});

		controls.add(selector);
		controls.add(playBtn);
		controls.add(stepBtn);
		controls.add(epochBtn);
		controls.add(resetBtn);
		controls.add(soundBtn);
		add(controls, BorderLayout.SOUTH);

		// timer for auto-playing steps - 50ms feels about right
		timer = new Timer(50, e -> {
			if (!panel.step())
				stop();
		});
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// toggle between play and pause
	private void togglePlay() {
		if (isPlaying)
			stop();
		else
			start();
	}

	private void start() {
		isPlaying = true;
		playBtn.setText("Pause");
		timer.start();
	}

	// stop everthing and kill the sound
	private void stop() {
		isPlaying = false;
		playBtn.setText("Play");
		timer.stop();
		panel.stopSound();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(SortingVisualizer::new);
	}
}

// the panel that actualy draws the bars and runs the sorting
class VisualizerPanel extends JPanel {
	private int[] array;
	private Sorter sorter;
	private final int size;
	private final SoundPlayer soundPlayer = new SoundPlayer();

	public VisualizerPanel(int size) {
		this.size = size;
		setPreferredSize(new Dimension(800, 400));
		setBackground(new Color(20, 20, 20));
		reset();
		setAlgorithm("Bubble Sort");
	}

	// randomize the aray
	public void reset() {
		array = new int[size];
		for (int i = 0; i < size; i++)
			array[i] = (int) (Math.random() * 300) + 50;
		if (sorter != null)
			sorter.init(array);
		repaint();
	}

	// swithc sorting algorithm based on dropdown
	public void setAlgorithm(String name) {
		sorter = switch (name) {
			case "Selection Sort" -> new SelectionSorter();
			case "Insertion Sort" -> new InsertionSorter();
			case "Merge Sort" -> new MergeSorter();
			case "Quick Sort" -> new QuickSorter();
			case "Heap Sort" -> new HeapSorter();
			default -> new BubbleSorter();
		};
		sorter.init(array);
		repaint();
	}

	// advance one step and play the sound for highlighted bar
	public boolean step() {
		boolean active = sorter.step();
		if (active) {
			int highlight = sorter.getHighlightedIndex();
			// bounds check so we dont crash (learned this the hard way)
			if (highlight >= 0 && highlight < array.length)
				soundPlayer.playSound(array[highlight]);
		}
		repaint();
		return active;
	}

	// skip ahead to the next epoch
	public void nextEpoch() {
		int currentEpoch = sorter.getEpoch();
		while (sorter.getEpoch() == currentEpoch && step())
			;
	}

	public void stopSound() {
		soundPlayer.stop();
	}

	// toggles mute on and off
	public boolean toggleMute() {
		return soundPlayer.toggleMute();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int barWidth = getWidth() / array.length;
		for (int i = 0; i < array.length; i++) {
			// magenta for highlighted, cyan for evrything else
			g.setColor(sorter.isHighlighted(i) ? Color.MAGENTA : Color.CYAN);
			g.fillRect(i * barWidth, getHeight() - array[i], barWidth - 1, array[i]);
		}
	}
}

// sound synth using pre-computed Clip objects for click free playback
// each bar hieght maps to a note on the C pentatonic scale across 3 octaves
// clips are generated once at startup so there are no buffer underruns
// this took forever to get working right omg
class SoundPlayer {
	private static final float SAMPLE_RATE = 44100f;
	private boolean muted = false;
	private Clip[] clips;

	// pentatonic scale (C-D-E-G-A) across 3 octves
	private static final double[] PENTATONIC_NOTES = { 261.63, 293.66, 329.63, 392.00, 440.00, // Octave 4
			523.25, 587.33, 659.25, 783.99, 880.00, // Octave 5
			1046.50, 1174.66, 1318.51, 1567.98, 1760.00 // Octave 6
	};

	// pre-generate all the clips at startup
	public SoundPlayer() {
		AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
		clips = new Clip[PENTATONIC_NOTES.length];

		for (int n = 0; n < PENTATONIC_NOTES.length; n++) {
			try {
				double freq = PENTATONIC_NOTES[n];
				// ~30ms tone - short enought to not drone
				int toneSamples = (int) (SAMPLE_RATE * 0.030);
				byte[] buf = new byte[toneSamples * 2];

				for (int i = 0; i < toneSamples; i++) {
					double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
					double sample = Math.sin(angle);

					// hann window for smooth fade-in/out, zero at endponts
					double t = (double) i / (toneSamples - 1);
					double envelope = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * t));
					sample *= envelope * 0.10; // very gentle volme

					// convert to 16-bit signed litle-endian
					short val = (short) (sample * Short.MAX_VALUE);
					buf[2 * i] = (byte) (val & 0xFF);
					buf[2 * i + 1] = (byte) ((val >> 8) & 0xFF);
				}

				clips[n] = AudioSystem.getClip();
				clips[n].open(af, buf, 0, buf.length);
			} catch (LineUnavailableException ignored) {
				// why does java audio have to be so painful
			}
		}
	}

	// play the tone coresponding to the bar value
	public void playSound(int value) {
		if (muted || clips == null)
			return;

		// map bar value (50-349) to a pentatonic note index
		int noteIndex = (int) ((value - 50) / 300.0 * (PENTATONIC_NOTES.length - 1));
		noteIndex = Math.max(0, Math.min(noteIndex, PENTATONIC_NOTES.length - 1));

		Clip clip = clips[noteIndex];
		if (clip != null) {
			clip.setFramePosition(0);
			clip.start();
		}
	}

	// stop all clips imediately
	public void stop() {
		if (clips != null) {
			for (Clip clip : clips) {
				if (clip != null)
					clip.stop();
			}
		}
	}

	// toggles mute state, returns true if now muted
	public boolean toggleMute() {
		muted = !muted;
		if (muted)
			stop();
		return muted;
	}
}

// --- SORTER STRATEGES ---

abstract class Sorter {
	protected int[] arr;
	protected int epoch = 0;

	// initalize the array and reset internal state
	public void init(int[] a) {
		this.arr = a;
		resetState();
		epoch = 0;
	}

	public abstract void resetState();

	public abstract boolean step();

	public abstract boolean isHighlighted(int index);

	public abstract int getHighlightedIndex();

	public int getEpoch() {
		return epoch;
	}

	// simple swap helper
	protected void swap(int i, int j) {
		int t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}
}

// bubble sort - the classic, the slow one
class BubbleSorter extends Sorter {
	private int i, j;

	public void resetState() {
		i = 0;
		j = 0;
	}

	// compare adjacent elemnts and swap if needed
	public boolean step() {
		if (i >= arr.length - 1)
			return false;
		if (arr[j] > arr[j + 1])
			swap(j, j + 1);
		j++;
		if (j >= arr.length - 1 - i) {
			j = 0;
			i++;
			epoch++;
		}
		return true;
	}

	public boolean isHighlighted(int idx) {
		return idx == j || idx == j + 1;
	}

	public int getHighlightedIndex() {
		return j;
	}
}

// selection sort - find the minimun each pass
class SelectionSorter extends Sorter {
	private int i, j, minIdx;

	public void resetState() {
		i = 0;
		j = 1;
		minIdx = 0;
	}

	// scan for smallest elemnt then swap it into place
	public boolean step() {
		if (i >= arr.length - 1)
			return false;
		if (arr[j] < arr[minIdx])
			minIdx = j;
		j++;
		if (j >= arr.length) {
			swap(i, minIdx);
			i++;
			minIdx = i;
			j = i + 1;
			epoch++;
		}
		return true;
	}

	public boolean isHighlighted(int idx) {
		return idx == j || idx == minIdx;
	}

	public int getHighlightedIndex() {
		return j < arr.length ? j : i;
	}
}

// insertion sort - this one is pretty straighforward
class InsertionSorter extends Sorter {
	private int i, j;

	public void resetState() {
		i = 1;
		j = 1;
	}

	// shift element left untill its in the right spot
	public boolean step() {
		if (i >= arr.length)
			return false;
		if (j > 0 && arr[j - 1] > arr[j]) {
			swap(j, j - 1);
			j--;
		} else {
			i++;
			j = i;
			epoch++;
		}
		return true;
	}

	public boolean isHighlighted(int idx) {
		return idx == j;
	}

	public int getHighlightedIndex() {
		return j;
	}
}

// merge sort - iterative bottom-up version
// had to look up how to do this iterativley
class MergeSorter extends Sorter {
	private int width, left;
	private int[] temp;

	public void resetState() {
		width = 1;
		left = 0;
		temp = new int[arr.length];
	}

	// merge pairs of subarrays, doubling width each pass
	public boolean step() {
		if (width >= arr.length)
			return false;
		int mid = Math.min(left + width, arr.length);
		int right = Math.min(left + 2 * width, arr.length);
		merge(left, mid, right);
		left += 2 * width;
		if (left >= arr.length) {
			left = 0;
			width *= 2;
			epoch++;
		}
		return true;
	}

	// the actual merge opreation
	private void merge(int l, int m, int r) {
		int i = l, j = m, k = l;
		while (i < m && j < r)
			temp[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
		while (i < m)
			temp[k++] = arr[i++];
		while (j < r)
			temp[k++] = arr[j++];
		System.arraycopy(temp, l, arr, l, r - l);
	}

	public boolean isHighlighted(int idx) {
		return idx >= left && idx < left + 2 * width;
	}

	public int getHighlightedIndex() {
		return left;
	}
}

// quick sort - using a stack instead of recursoin
// not working...
// still not working
// ok i think its the pivot selction??
// nvm fixed it was the partiton bounds
class QuickSorter extends Sorter {
	private Stack<int[]> stack;
	private int i, j, pivot, low, high;
	private boolean partitioning;

	public void resetState() {
		stack = new Stack<>();
		stack.push(new int[] { 0, arr.length - 1 });
		partitioning = false;
	}

	// lomuto partition scheme - one step at a time
	public boolean step() {
		if (!partitioning) {
			if (stack.isEmpty())
				return false;
			int[] r = stack.pop();
			low = r[0];
			high = r[1];
			if (low >= high)
				return step(); // skip trivial ranges
			pivot = arr[high];
			i = low;
			j = low;
			partitioning = true;
		}
		if (j < high) {
			if (arr[j] < pivot) {
				swap(i, j);
				i++;
			}
			j++;
		} else {
			swap(i, high);
			stack.push(new int[] { low, i - 1 });
			stack.push(new int[] { i + 1, high });
			partitioning = false;
			epoch++;
		}
		return true;
	}

	public boolean isHighlighted(int idx) {
		return idx == j || idx == high;
	}

	public int getHighlightedIndex() {
		return j;
	}
}

// heap sort - build max heap then extract elemnts
// i keep forgetting how heapify works every singel time
class HeapSorter extends Sorter {
	private int n, i;
	private boolean building;

	public void resetState() {
		n = arr.length;
		i = n / 2 - 1;
		building = true;
	}

	// two phases: build the heap, then extract max repeatdly
	public boolean step() {
		if (building) {
			heapify(arr, n, i);
			i--;
			if (i < 0) {
				building = false;
				i = n - 1;
			}
		} else {
			if (i <= 0)
				return false;
			swap(0, i);
			heapify(arr, i, 0);
			i--;
			epoch++;
		}
		return true;
	}

	// sift down to maintan heap property
	// had to draw this out on paper to get it right lol
	private void heapify(int[] a, int n, int i) {
		int largest = i, l = 2 * i + 1, r = 2 * i + 2;
		if (l < n && a[l] > a[largest])
			largest = l;
		if (r < n && a[r] > a[largest])
			largest = r;
		if (largest != i) {
			swap(i, largest);
			heapify(a, n, largest);
		}
	}

	public boolean isHighlighted(int idx) {
		return idx == i || idx == 0;
	}

	public int getHighlightedIndex() {
		return i;
	}
}

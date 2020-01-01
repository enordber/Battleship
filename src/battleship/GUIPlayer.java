package battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUIPlayer extends UIPlayer implements ActionListener, ChangeListener {
	private static final Color HIT_COLOR = Color.RED;
	private static final Color TARGET_GRID_MISS_COLOR = Color.WHITE;
	private static final Color TARGET_GRID_FIELD_COLOR = Color.CYAN;
	private static final Color OCEAN_GRID_FIELD_COLOR = Color.BLUE;
	private static final Color OCEAN_GRID_MISS_COLOR = Color.LIGHT_GRAY;
	private static final Color BACKGROUND_COLOR = Color.WHITE;
	private static final Color SALVO_COLOR = Color.GREEN;

	private static final String PLACE_SHIPS_LABEL = "Place Ships";
	private static final String NEW_GAME_LABEL = "New Game";
	private static final String SALVO_TOGGLE_LABEL = "Salvo";

	private JFrame frame;
	private JComponent[][] oceanGridCells;
	private JLabel playerPreviousShotLabel;
	private JLabel opponentPreviousShotLabel;
	private JLabel difficultyLabel;
	private double difficultySetting = 0.4;
	private JPanel statusPanel;
	private MouseListener mouseListener;

	private GameMode mode = GameMode.BATTLESHIP;
	private int salvoSize = 1;
	private ArrayList<int[]> salvoShots = new ArrayList<int[]>(salvoSize);
	private ArrayList<JComponent> salvoCellComponents = new ArrayList<JComponent>(salvoSize);

	GUIPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	/**
	 * Creates the GUI, reusing the frame if it has already been created.
	 */
	private void createGUI() {
		if(frame == null) {
			frame = new JFrame("Battleship");
			frame.setLocation(750, 0); //for temporary dev convenience
		}
		frame.setContentPane(new JPanel());
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().setBackground(BACKGROUND_COLOR);
		frame.getContentPane().add(getGridPanel());
		playerPreviousShotLabel = new JLabel(" ");
		opponentPreviousShotLabel = new JLabel(" ");
		statusPanel = new JPanel();
		statusPanel.setBackground(BACKGROUND_COLOR);
		statusPanel.add(getStatusPanel());
		frame.getContentPane().add(statusPanel);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * The panel holding the two game grids.
	 * @return
	 */
	private JPanel getGridPanel() {
		JPanel gridPanel = new JPanel();
		gridPanel.setBackground(BACKGROUND_COLOR);
		gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
		gridPanel.add(getTargetGridPanel(), BorderLayout.NORTH);
		JPanel spacerPanel = new JPanel();
		spacerPanel.setBackground(BACKGROUND_COLOR);
		spacerPanel.setMinimumSize(new Dimension(40,40));
		gridPanel.add(spacerPanel);
		gridPanel.add(getOceanGridPanel(), BorderLayout.SOUTH);
		
		spacerPanel = new JPanel();
		spacerPanel.setBackground(BACKGROUND_COLOR);
		spacerPanel.setMinimumSize(new Dimension(40,40));
		gridPanel.add(spacerPanel);
		gridPanel.add(getControlPanel());
		return gridPanel;
	}

	/**
	 * Creates and returns a panel with UI controls.
	 * 
	 * @return
	 */
	private JPanel getControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.setBackground(BACKGROUND_COLOR);
		JButton placeShipsButton = new JButton(PLACE_SHIPS_LABEL);
		placeShipsButton.addActionListener(this);
		//		controlPanel.add(placeShipsButton);
		
		JToggleButton salvoToggle = new JToggleButton(SALVO_TOGGLE_LABEL);
		salvoToggle.setSelected(getGameMode() == GameMode.SALVO);
		salvoToggle.addActionListener(this);
		controlPanel.add(salvoToggle);

		JButton newGameButton = new JButton(NEW_GAME_LABEL);
		newGameButton.addActionListener(this);
		controlPanel.add(newGameButton);

		if(getGame().getOpponent() instanceof ProbabilityPlayer) {
			//add difficulty slider to control probability setting of 
			//ProbabilityPlayer
			ProbabilityPlayer opponent = (ProbabilityPlayer)getGame().getOpponent();
			opponent.setHitProbability(difficultySetting);
			int initialValue = (int)(difficultySetting * 100);

			JPanel difficultyPanel = new JPanel();
			difficultyPanel.setBackground(BACKGROUND_COLOR);
			difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.Y_AXIS));

			difficultyLabel = new JLabel("Difficulty: " + opponent.getHitProbability());

			JSlider difficultySlider = new JSlider(SwingConstants.HORIZONTAL);
			difficultySlider.setMinimum(0);
			difficultySlider.setMaximum(100);
			difficultySlider.setValue(initialValue);
			difficultySlider.addChangeListener(this);

			difficultyPanel.add(difficultyLabel);
			difficultyPanel.add(difficultySlider);
			controlPanel.add(difficultyPanel);
		}

		return controlPanel;
	}

	/**
	 * Creates and returns the panel containing the ocean grid,
	 * representing the player's field.
	 * 
	 * @return
	 */
	private JPanel getOceanGridPanel() {
		JPanel cellPanel = new JPanel();
		cellPanel.setLayout(new GridLayout(getOceanGridRowCount(), getOceanGridColumnCount(), 2, 2));
		Ship[][] oceanGrid = getOceanGrid();
		oceanGridCells = new JComponent[oceanGrid.length][oceanGrid[0].length];
		for(int i = 0; i < oceanGrid.length; i++) {
			for(int j = 0; j < oceanGrid[i].length; j++) {
				String label = BattleshipGame.rowLabels.get(i) + BattleshipGame.columnLabels.get(j);
				JComponent cellComponent = getCellComponent(label);
				oceanGridCells[i][j] = cellComponent;
				switch(oceanGrid[i][j].getType()) {
				case UNKNOWN: 
					cellComponent.setBackground(TARGET_GRID_FIELD_COLOR);
					break;
				case NONE:
					cellComponent.setBackground(OCEAN_GRID_FIELD_COLOR);
					break;
				default:
					cellComponent.setBackground(TARGET_GRID_MISS_COLOR);
					cellComponent.add(new JLabel(oceanGrid[i][j].toString().substring(0, 1)));
				}
				cellPanel.add(cellComponent);
			}
		}

		return cellPanel;
	}

	/**
	 * Creates and returns the panel containing the target grid,
	 * representing the opponent's field.
	 * 
	 * @return
	 */
	private JPanel getTargetGridPanel() {
		JPanel targetGridPanel = new JPanel();
		targetGridPanel.setLayout(new GridLayout(getTargetGridRowCount(), getTargetGridColumnCount(), 2, 2));
		Ship[][] targetGrid = getTargetGrid();
		MouseListener mouseAdapter = getMouseListener();

		for(int i = 0; i < targetGrid.length; i++) {
			for(int j = 0; j < targetGrid[i].length; j++) {
				String label = BattleshipGame.rowLabels.get(i) + BattleshipGame.columnLabels.get(j);
				JComponent cellComponent = getCellComponent(label);
				switch(targetGrid[i][j].getType()) {
				case UNKNOWN: 
					cellComponent.setBackground(TARGET_GRID_FIELD_COLOR);
					break;
				case NONE:
					cellComponent.setBackground(TARGET_GRID_MISS_COLOR);
					break;
				default:
					cellComponent.setBackground(HIT_COLOR);
				}
				cellComponent.addMouseListener(mouseAdapter);
				targetGridPanel.add(cellComponent);
			}
		}

		return targetGridPanel;
	}

	private MouseListener getMouseListener() {
		if(mouseListener == null) {
			mouseListener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JComponent cellComponent = (JComponent)e.getComponent();
					switch(getGameMode()) {
					case BATTLESHIP:
					case SALVO:
						clickOnTargetCell(cellComponent);
					}
				}
			};
		}

		return mouseListener;
	}

	/**
	 * Creates and returns a grid cell component with the given label.
	 * The label is the coordinates as they would be called out in 
	 * gameplay (e.g. E4, C7).
	 * 
	 * @param label Cell coordinates as they would be called out in 
	 * gameplay (e.g. E4, C7)
	 * @return
	 */
	private JComponent getCellComponent(String label) {
		Dimension cellSize = new Dimension(24,24);
		JComponent r = new JPanel();
		r.setName(label);
		r.setMaximumSize(cellSize);
		r.setMinimumSize(cellSize);
		r.setSize(cellSize);
		r.setPreferredSize(cellSize);
		r.setBorder(new BevelBorder(BevelBorder.RAISED));

		return r;
	}

	/**
	 * Creates and returns the status panel with information about
	 * player and opponent ships.
	 * 
	 * @return status panel
	 */
	private JPanel getStatusPanel() {
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		statusPanel.setLayout(new GridLayout(0, 1));
		statusPanel.setBackground(Color.WHITE);

		JPanel targetStatusPanel = new JPanel();
		targetStatusPanel.setLayout(new BoxLayout(targetStatusPanel, BoxLayout.Y_AXIS));
		targetStatusPanel.setBackground(Color.WHITE);
		JPanel oceanStatusPanel = new JPanel();
		oceanStatusPanel.setLayout(new BoxLayout(oceanStatusPanel, BoxLayout.Y_AXIS));
		oceanStatusPanel.setBackground(Color.WHITE);

		statusPanel.add(targetStatusPanel);
		statusPanel.add(oceanStatusPanel);

		targetStatusPanel.add(new JLabel("Opponent Ship Status"));
		targetStatusPanel.add(getShipStatusPanel(getGame().getOpponent().getShips()));

		oceanStatusPanel.add(new JLabel("Player Ship Status"));
		oceanStatusPanel.add(getShipStatusPanel(getShips()));
		JPanel previousShotPanel = new JPanel();
		previousShotPanel.setLayout(new BoxLayout(previousShotPanel, BoxLayout.Y_AXIS));
		previousShotPanel.setBackground(Color.WHITE);

		JPanel playerShotPanel = new JPanel();
		playerShotPanel.setLayout(new BoxLayout(playerShotPanel, BoxLayout.Y_AXIS));
		playerShotPanel.add(new JLabel("Player Previous Shot: "));
		playerShotPanel.add(playerPreviousShotLabel);
		playerShotPanel.setBackground(Color.WHITE);
		targetStatusPanel.add(playerShotPanel);

		JPanel opponentShotPanel = new JPanel();
		opponentShotPanel.setLayout(new BoxLayout(opponentShotPanel, BoxLayout.Y_AXIS));
		opponentShotPanel.add(new JLabel("Opponent Previous Shot: "));
		opponentShotPanel.add(opponentPreviousShotLabel);
		opponentShotPanel.setBackground(Color.WHITE);
		oceanStatusPanel.add(opponentShotPanel);

		statusPanel.add(previousShotPanel);
		return statusPanel;
	}

	/**
	 * Create a panel showing the status of the given Ships.
	 * 
	 * @param ships
	 * @return
	 */
	private JPanel getShipStatusPanel(ArrayList<Ship> ships) {
		JPanel shipStatusPanel = new JPanel();
		shipStatusPanel.setLayout(new BoxLayout(shipStatusPanel, BoxLayout.Y_AXIS));
		shipStatusPanel.setBackground(Color.WHITE);

		String[] columnNames = new String[]{"Ship Type", "Hits", "Sunk"};
		Object[][] tableData = new Object[ships.size()][3];
		for(int i = 0; i < tableData.length; i++) {
			Ship ship = ships.get(i);
			tableData[i][0] = ship.getType();
			tableData[i][1] = ship.getHitCount() + " of " + ship.getSize();
			tableData[i][2] = ship.isSunk();
		}

		JTable shipStatusTable = new JTable(tableData, columnNames);
		shipStatusTable.setPreferredScrollableViewportSize(new Dimension(
				shipStatusTable.getPreferredSize().width,
				shipStatusTable.getRowHeight() * ships.size()));

		shipStatusTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		JScrollPane scrollPane = new JScrollPane(shipStatusTable);
		shipStatusPanel.add(scrollPane);

		return shipStatusPanel;
	}

	/**
	 * Places ships, creates the GUI, and begins gameplay.
	 */
	@Override
	void play(BattleshipGame game) {
		setGame(game);
		placeShips();
		updateSalvoSize();
		createGUI();
		
	}

	/**
	 * Handles a mouse click on the given cellComponent of the Target Grid.
	 * 
	 * @param cellComponent The component that was clicked.
	 */
	private void clickOnTargetCell(JComponent cellComponent) {
		if(salvoCellComponents.contains(cellComponent)) {
			int index = salvoCellComponents.indexOf(cellComponent);
			salvoCellComponents.remove(index);
			salvoShots.remove(index);
			cellComponent.setBackground(TARGET_GRID_FIELD_COLOR);
		} else {
			String cellLabel = cellComponent.getName();
			int[] position = parseForShotPosition(cellLabel);
			salvoShots.add(position);
			salvoCellComponents.add(cellComponent);
			cellComponent.setBackground(SALVO_COLOR);
			getTargetGrid()[position[rowIndex]][position[columnIndex]] = Ship.SALVO_TARGET;

			if(salvoShots.size() == getSalvoSize()) {
				fireShots();
			}
		}
	}

	/**
	 * Fire all shots currently in the salvo queue, then calls for
	 * the next incoming shot(s).
	 */
	private void fireShots() {
		for(int i = 0; i < salvoShots.size(); i++) {
			int[] position = salvoShots.get(i);
			JComponent cellComponent = salvoCellComponents.get(i);
			String cellLabel = cellComponent.getName();
			Ship opponentShipAtPosition = 
					getGame().shootAt(position[columnIndex], position[rowIndex]);
			cellComponent.setForeground(Color.BLACK);
			if(opponentShipAtPosition.getType() == ShipType.NONE) {
				playerPreviousShotLabel.setText(cellLabel + " Miss");
				cellComponent.setBackground(TARGET_GRID_MISS_COLOR);
			} else {
				playerPreviousShotLabel.setText(cellLabel + " Hit on " + opponentShipAtPosition.getType());
				cellComponent.setBackground(HIT_COLOR);
				cellComponent.add(new JLabel(opponentShipAtPosition.getType().toString().substring(0, 1)));
			}
			registerShotOnTargetResults(position, opponentShipAtPosition);	
			cellComponent.removeMouseListener(getMouseListener());
			cellComponent.setBorder(new BevelBorder(BevelBorder.LOWERED));
			updateStatus();
		}

		VictoryStatus victoryStatus = getGame().evaluateVictory();
		if(victoryStatus != VictoryStatus.UNDECIDED) {
			endGame();
		} else {		
			acceptNextShot();
			updateStatus();
			victoryStatus = getGame().evaluateVictory();
			if(victoryStatus != VictoryStatus.UNDECIDED) {
				endGame();
			}
		}
		salvoShots.clear();
		salvoCellComponents.clear();
		updateSalvoSize();
	}

	/**
	 * Sets the current salvo size based on game mode and number of 
	 * surviving ships.
	 */
	private void updateSalvoSize() {
		int size = 1;
		if(getGameMode() == GameMode.SALVO) {
			size = 0;
			for(Ship ship: getShips()) {
				if(!ship.isSunk()) {
					size++;
				}
			}
		}
		setSalvoSize(size);
	}

	/**
	 * Calls for the next incoming shot from the opponent and registers the
	 * result.
	 */
	private void acceptNextShot() {
		if(getGameMode() == GameMode.BATTLESHIP) {
			int[] incomingShotPosition = getGame().getNextShotPosition();
			acceptNextShot(incomingShotPosition);
		} else if(getGameMode() == GameMode.SALVO) {
			ArrayList<int[]> incomingSalvo = getGame().getNextSalvoPositions();
			for(int[] incomingShotPosition: incomingSalvo) {
				acceptNextShot(incomingShotPosition);
			}
		}
	}

	/**
	 * Registers result of next incoming shot.
	 */
	private void acceptNextShot(int[] incomingShotPosition) {
		String cellLabel = BattleshipGame.rowLabels.get(incomingShotPosition[rowIndex]) +
				BattleshipGame.columnLabels.get(incomingShotPosition[columnIndex]);
		Ship ownShipAtPosition = shotAt(incomingShotPosition[columnIndex], 
				incomingShotPosition[rowIndex]);
		if(ownShipAtPosition.getType() == ShipType.NONE) {
			opponentPreviousShotLabel.setText(cellLabel + " Miss");
			oceanGridCells[incomingShotPosition[rowIndex]][incomingShotPosition[columnIndex]].setBackground(OCEAN_GRID_MISS_COLOR);
		} else {
			opponentPreviousShotLabel.setText(cellLabel + " Hit on " + ownShipAtPosition.getType());
			oceanGridCells[incomingShotPosition[rowIndex]][incomingShotPosition[columnIndex]].setBackground(HIT_COLOR);
		}
		oceanGridCells[incomingShotPosition[rowIndex]][incomingShotPosition[columnIndex]].setBorder(new BevelBorder(BevelBorder.LOWERED));
		//send back result of the shot from opponent
		getGame().registerShotOnTargetResults(incomingShotPosition, ownShipAtPosition);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case PLACE_SHIPS_LABEL:
			placeShips();
			break;
		case NEW_GAME_LABEL:
			newGame();
			break;
		case SALVO_TOGGLE_LABEL:
			if(((AbstractButton)e.getSource()).isSelected()) {
				setGameMode(GameMode.SALVO);
			} else {
				setGameMode(GameMode.BATTLESHIP);
			}
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source instanceof JSlider) {
			JSlider slider = (JSlider) source;
			int value = slider.getValue();
			difficultySetting = value/100.0;
			ProbabilityPlayer opponent = (ProbabilityPlayer)getGame().getOpponent();
			opponent.setHitProbability(difficultySetting);
			difficultyLabel.setText("Difficulty: " + opponent.getHitProbability());
		}
	}

	private void newGame() {
		getGame().newGame();
	}

	private void endGame() {
		setGameMode(GameMode.GAME_OVER);
		VictoryStatus victoryStatus = getGame().evaluateVictory();
		String message = "Game Over.";
		if(victoryStatus == VictoryStatus.PLAYER_VICTORY) {
			message += " You Win.";
		} else {
			message += " You Lose.";
		}
		JOptionPane.showMessageDialog(frame, message);
	}

	/**
	 * Refresh the Status panel.
	 */
	private void updateStatus() {
		statusPanel.removeAll();
		statusPanel.add(getStatusPanel());
	}
	
	private GameMode getGameMode() {
		return mode;
	}
	
	private void setGameMode(GameMode mode) {
		this.mode = mode;
		clearSalvoQueue();
		updateSalvoSize();
	}
	
	private int getSalvoSize() {
		return salvoSize;
	}
	
	private void setSalvoSize(int salvoSize) {
		this.salvoSize = salvoSize;
	}
	
	private void clearSalvoQueue() {
		for(JComponent cellComponent: salvoCellComponents) {
			cellComponent.setBackground(TARGET_GRID_FIELD_COLOR);
		}
		salvoCellComponents.clear();
		salvoShots.clear();
	}

}

package battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

public class GUIPlayer extends UIPlayer implements MouseListener, ActionListener {
	private static final Color HIT_COLOR = Color.RED;
	private static final Color MISS_COLOR = Color.WHITE;
	private static final Color TARGET_GRID_FIELD_COLOR = Color.CYAN;
	private static final Color OCEAN_GRID_FIELD_COLOR = Color.BLUE;

	private static final String PLACE_SHIPS_LABEL = "Place Ships";
	private static final String NEW_GAME_LABEL = "New Game";

	private JFrame frame;
	private JComponent[][] oceanGridCells;
	private JLabel playerPreviousShotLabel;
	private JLabel opponentPreviousShotLabel;
	private JPanel statusPanel;
	
	private GameMode mode = GameMode.GAME_OVER;

	GUIPlayer(int oceanGridRowCount, int oceanGridColumnCount,
			int targetGridRowCount, int targetGridColumnCount) {
		super(oceanGridRowCount, oceanGridColumnCount, targetGridRowCount,
				targetGridColumnCount);
	}

	private void createGUI() {
		if(frame == null) {
			frame = new JFrame("Battleship");
		}
		frame.setContentPane(new JPanel());
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().add(getGridPanel());
		playerPreviousShotLabel = new JLabel();
		opponentPreviousShotLabel = new JLabel();
		statusPanel = new JPanel();
		statusPanel.add(getStatusPanel());
		frame.getContentPane().add(statusPanel);
		frame.pack();
		frame.setLocation(750, 0); //for temporary dev convenience
		frame.setVisible(true);
	}

	private JPanel getGridPanel() {
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
		gridPanel.add(getTargetGridPanel(), BorderLayout.NORTH);
		JPanel spacerPanel = new JPanel();
		spacerPanel.setMinimumSize(new Dimension(10,10));
		gridPanel.add(spacerPanel);
		gridPanel.add(getOceanGridPanel(), BorderLayout.SOUTH);
		gridPanel.add(spacerPanel);
		gridPanel.add(getControlPanel());
		return gridPanel;
	}

	private JPanel getControlPanel() {
		JPanel controlPanel = new JPanel();
		JButton placeShipsButton = new JButton(PLACE_SHIPS_LABEL);
		placeShipsButton.addActionListener(this);
//		controlPanel.add(placeShipsButton);

		JButton newGameButton = new JButton(NEW_GAME_LABEL);
		newGameButton.addActionListener(this);
		controlPanel.add(newGameButton);
		return controlPanel;
	}

	private JPanel getOceanGridPanel() {
		JPanel oceanGridPanel = new JPanel();
		oceanGridPanel.setLayout(new GridLayout(10, 10, 1, 1));
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
					cellComponent.setBackground(MISS_COLOR);
					cellComponent.add(new JLabel(oceanGrid[i][j].toString().substring(0, 1)));
				}
				oceanGridPanel.add(cellComponent);
			}
		}

		return oceanGridPanel;
	}

	private JPanel getTargetGridPanel() {
		JPanel targetGridPanel = new JPanel();
		targetGridPanel.setLayout(new GridLayout(10, 10, 1, 1));
		Ship[][] targetGrid = getTargetGrid();
		for(int i = 0; i < targetGrid.length; i++) {
			for(int j = 0; j < targetGrid[i].length; j++) {
				String label = BattleshipGame.rowLabels.get(i) + BattleshipGame.columnLabels.get(j);
				JComponent cellComponent = getCellComponent(label);
				switch(targetGrid[i][j].getType()) {
				case UNKNOWN: 
					cellComponent.setBackground(TARGET_GRID_FIELD_COLOR);
					break;
				case NONE:
					cellComponent.setBackground(MISS_COLOR);
					break;
				default:
					cellComponent.setBackground(HIT_COLOR);
				}
				cellComponent.addMouseListener(this);
				targetGridPanel.add(cellComponent);
			}
		}
		return targetGridPanel;
	}

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

	private JPanel getStatusPanel() {
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
//		statusPanel.setLayout(new GridLayout(2, 1));
		statusPanel.setBackground(Color.WHITE);
//		statusPanel.setMinimumSize(new Dimension(300,800));

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
		
		JScrollPane scrollPane = new JScrollPane(shipStatusTable);
		shipStatusPanel.add(scrollPane);

		return shipStatusPanel;
	}

	@Override
	void play(BattleshipGame game) {
		setGame(game);
		placeShips();
		createGUI();
		mode = GameMode.GAMEPLAY;
	}

	private void clickOnTargetCell(JComponent cellComponent) {
		String cellLabel = cellComponent.getName();
		int[] position = parseForShotPosition(cellLabel);
		Ship opponentShipAtPosition = 
				getGame().shootAt(position[columnIndex], position[rowIndex]);
		cellComponent.setForeground(Color.BLACK);
		if(opponentShipAtPosition.getType() == ShipType.NONE) {
			playerPreviousShotLabel.setText(cellLabel + " Miss");
			cellComponent.setBackground(MISS_COLOR);
		} else {
			playerPreviousShotLabel.setText(cellLabel + " Hit on " + opponentShipAtPosition.getType());
			cellComponent.setBackground(HIT_COLOR);
			cellComponent.add(new JLabel(opponentShipAtPosition.getType().toString().substring(0, 1)));
			cellComponent.revalidate();
		}
		registerShotOnTargetResults(position, opponentShipAtPosition);	
		cellComponent.removeMouseListener(this);
		updateStatus();

		VictoryStatus victoryStatus = getGame().evaluateVictory();
		if(victoryStatus != VictoryStatus.UNDECIDED) {
			//TODO - GUI notification
			System.out.println("Game Over. " + victoryStatus);
			endGame();
		} else {		
			acceptNextShot();
			updateStatus();
			victoryStatus = getGame().evaluateVictory();
			if(victoryStatus != VictoryStatus.UNDECIDED) {
				//TODO - GUI notification
				System.out.println("Game Over. " + victoryStatus);
				endGame();
			}
		}
	}

	private void acceptNextShot() {
		int[] incomingShotPosition = getGame().getNextShotPosition();
		String cellLabel = BattleshipGame.rowLabels.get(incomingShotPosition[rowIndex]) +
				BattleshipGame.columnLabels.get(incomingShotPosition[columnIndex]);
		System.out.print("Incoming shot at " + cellLabel + "... ");
		Ship ownShipAtPosition = shotAt(incomingShotPosition[columnIndex], 
				incomingShotPosition[rowIndex]);
		if(ownShipAtPosition.getType() == ShipType.NONE) {
			opponentPreviousShotLabel.setText(cellLabel + " Miss");
			System.out.println("Miss");
		} else {
			System.out.println("Hit on " + ownShipAtPosition);
			opponentPreviousShotLabel.setText(cellLabel + " Hit on " + ownShipAtPosition.getType());
			oceanGridCells[incomingShotPosition[rowIndex]][incomingShotPosition[columnIndex]].setBackground(HIT_COLOR);
		}
		//send back result of the shot from opponent
		getGame().registerShotOnTargetResults(incomingShotPosition, ownShipAtPosition);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		JComponent cellComponent = (JComponent)e.getComponent();
		if(mode == GameMode.GAMEPLAY) {
			clickOnTargetCell(cellComponent);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
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
		}
	}

	private void newGame() {
		getGame().newGame();
	}
	
	private void endGame() {
		mode = GameMode.GAME_OVER;
	}

	private void updateStatus() {
		statusPanel.removeAll();
		statusPanel.add(getStatusPanel());
	}
}

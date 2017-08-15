package visualisation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class GraphViewImp extends JFrame implements GraphView {
	private JTable table;
	//private JButton addColumnBtn;
	
	public GraphViewImp(TableModel tableModel) {
		setLayout(new FlowLayout());
		TableCellRenderer colorRenderer = new ColorRenderer();
		table = new JTable(tableModel){
			public TableCellRenderer getCellRenderer(int row, int column) {
				if ((column != 0)) {
		        	return colorRenderer;
		        }
		        return super.getCellRenderer(row, column);
		    }
		};
		table.setFillsViewportHeight(true);
		JScrollPane pane = new JScrollPane(table);
		
		/*addColumnBtn = new JButton("Change Schedule");
		addColumnBtn.setActionCommand(GraphController.NEW_SCHEDULE);
		
		JPanel south = new JPanel();
		south.add(addColumnBtn);*/
		
		add(pane, BorderLayout.CENTER);
		//add(south, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 520);
		setVisible(true);
	}

	/*public void addButtonListener(ActionListener listener) {
		addColumnBtn.addActionListener(listener);
	}*/
	
	public JTable getTable(){
		return table;
	}

	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}
}

package visualisation;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import implementations.algorithm.AlgorithmImp;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;

public class TableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	// set up dummy data:

	private String[] _columnNames; // initialize based on number of processors.

	private String[][] _data; // initalize based on schedular time object.
	private int _cores;
	private int _bestTime;// total best time possible - will represent number of rows.
	private HashMap<String, NodeSchedule> map;
	private DAG _dag;
	
	public TableModel(AlgorithmImp alg, DAG dag, int cores){
		_cores = cores;
		_bestTime = alg.getBestTotalTime();
		map = alg.getCurrentBestSchedule();
		_dag = dag;
		_columnNames = initColumnNames();
		_data = initData();
	}
	
	public String[] initColumnNames(){
		String[] colNames = new String[_cores+1];
		colNames[0] = "Time";
		for (int i =1; i < _cores+1;i++){
			colNames[i] = "P" + i;
		}
		return colNames;
	}
	/**
	 * Method converts the bestSchedule HashMap to a 2D array, preparing it to be passed into a JTable.
	 * Effectively, the adapter method.
	 * @return
	 */
	public String[][] initData(){
		String[][] data = new String[_bestTime][_cores+1];
		
		// Initializing array with time values and empty strings.
		for (int i =0; i < _bestTime;i++){
			data[i][0] = (i + 1) + "";
			data[i][1] = "";
			data[i][2] = "";
		}
		
		for (Entry<String, NodeSchedule> entry : map.entrySet()) {
			
		    String key = entry.getKey();
		    NodeSchedule value = entry.getValue();
		    int startTime = value.getBestStartTime();
		    int core = value.getBestProcessor();
		    int nodeWeight = _dag.getNodeByName(key).getWeight();
		    
		    // go through all rows which have the same node based on node weight.
		    for (int i =0;i<nodeWeight;i++){
		    	data[startTime+i][core] = key.toUpperCase();
		    }
		}
		
		return data;
	}
	
	@Override
	public int getRowCount() {
		return _data.length;
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}


	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		return _data[rowIndex][columnIndex];
	}
	
	@Override
	public String getColumnName(int col) {
	    return _columnNames[col];
	}
	
	/*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        _data[row][col] = (String) value;
        fireTableCellUpdated(row, col);
    }
    
    public boolean isCellEditable(int row, int col){ 
    	return false; 
    }
    
    public void changeData(String[][] newdata){
    	_data = newdata;
    }

}

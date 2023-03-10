package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.BlockingProbability;
import measurement.Measurement;
import network.Pair;
import simulationControl.Util;

/**
 * This class is responsible for formatting the file with results of circuit blocking probability
 * 
 * @author Iallen
 */
public class BlockingProbResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, BlockingProbability>> bps; // Contains the blocking probability metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private List<Pair> pairs;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		bps = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, BlockingProbability>  reps = new HashMap<>();
			bps.put(load, reps);
			
			for (Measurement bp : loadPoint) {
				reps.put(bp.getReplication(), (BlockingProbability)bp);
			}			
		}
		BlockingProbability bp = (BlockingProbability) llms.get(0).get(0);
		loadPoints = new ArrayList<>(bps.keySet());
		replications = new ArrayList<>(bps.values().iterator().next().keySet());
		pairs = new ArrayList<>(bp.getUtil().pairs);
	}
	
	/**
	 * Returns a string corresponding to the result file for blocking probabilities
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Bandwidth" + sep + "src" + sep + "dest" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeneral());
		res.append("\n\n");
		res.append(resultGeneralLackTx());
		res.append("\n\n");
		res.append(resultGeneralLackRx());
		res.append("\n\n");
		res.append(resultGeneralFrag());
		res.append("\n\n");
		res.append(resultGeneralQoTN());
		res.append("\n\n");
		res.append(resultGeneralQoTO());
		res.append("\n\n");
		res.append(resultGeneralOther());
		res.append("\n\n");
		res.append(resultGeneralXt());
		res.append("\n\n");
		res.append(resultGeneralXtInOther());
		res.append("\n\n");
		
		res.append(blockAllCores());
		res.append("\n\n");
		res.append(blockCore0());
		res.append("\n\n");
		res.append(blockCore1());
		res.append("\n\n");
		res.append(blockCore2());
		res.append("\n\n");
		res.append(blockCore3());
		res.append("\n\n");
		res.append(blockCore4());
		res.append("\n\n");
		res.append(blockCore5());
		res.append("\n\n");
		res.append(blockCore6());
		res.append("\n\n");
		
		res.append(resultPair());
		res.append("\n\n");
		res.append(resultBandwidth());
		res.append("\n\n");
		res.append(resultPairBandwidth());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability general
	 * 
	 * @return String
	 */
	private String resultGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getGeneralBlockProb());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by lack of transmitters
	 * 
	 * @return String
	 */
	private String resultGeneralLackTx(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by lack of transmitters" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getReqBlockByLackTx());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by lack of receivers
	 * 
	 * @return String
	 */
	private String resultGeneralLackRx(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by lack of receivers" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getReqBlockByLackRx());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by fragmentation
	 * 
	 * @return String
	 */
	private String resultGeneralFrag(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by fragmentation" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getBlockProbByFragmentation());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by QoTN
	 * 
	 * @return String
	 */
	private String resultGeneralQoTN(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by QoTN" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getRegBlockByQoTN());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by QoTO
	 * 
	 * @return String
	 */
	private String resultGeneralQoTO(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by QoTO" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getRegBlockByQoTO());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by other
	 * 
	 * @return String
	 */
	private String resultGeneralOther(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by other" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getRegBlockByOther());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by crosstalk
	 * 
	 * @return String
	 */
	private String resultGeneralXt(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by crosstalk" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getRegBlockByXt());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability by crosstalk in others
	 * 
	 * @return String
	 */
	private String resultGeneralXtInOther(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Blocking probability by crosstalk in other" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getRegBlockByXtInOther());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Returns the blocking probability per pair
	 * 
	 * @return String
	 */
	private String resultPair(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Blocking probability per pair" + sep + loadPoint + sep + "all";
			
			for (Pair pair : this.pairs) {
				String aux2 = aux + sep + pair.getSource().getName() + sep + pair.getDestination().getName() + sep + " ";
				for (Integer replic : replications) {
					aux2 = aux2 + sep + bps.get(loadPoint).get(replic).getProbBlockPair(pair);
				}
				res.append(aux2 + "\n");		
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability per bandwidth
	 * 
	 * @return String
	 */
	private String resultBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Blocking probability per bandwidth" + sep + loadPoint;
			
			for (Double bandwidth : bps.get(0).get(0).getUtil().bandwidths) {
				String aux2 = aux + sep + (bandwidth/1000000000.0) + "Gbps" + sep + "all" + sep + "all" + sep + " ";
				for (Integer rep : replications) {
					aux2 = aux2 + sep + bps.get(loadPoint).get(rep).getProbBlockBandwidth(bandwidth);
				}
				res.append(aux2 + "\n");
			}	
		}
		return res.toString();
	}

	/**
	 * Returns the blocking probability per pair and bandwidth
	 * 
	 * @return String
	 */
	private String resultPairBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Blocking probability per pair and bandwidth" + sep + loadPoint;
			
			for (Double bandwidth : bps.get(0).get(0).getUtil().bandwidths) {
				String aux2 = aux + sep + (bandwidth/1000000000.0) + "Gbps";
				for (Pair pair :  bps.get(0).get(0).getUtil().pairs) {
					String aux3 = aux2 + sep + pair.getSource().getName() + sep + pair.getDestination().getName() + sep + " ";
					for(Integer rep :  replications){
						aux3 = aux3 + sep + bps.get(loadPoint).get(rep).getProbBlockPairBandwidth(pair, bandwidth);
					}
					res.append(aux3 + "\n");
				}				
			}
		}
		return res.toString();
	}
	
	private String blockAllCores(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block all cores" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockAllCores());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore0(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 0 " + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore0());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore1(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 1 " + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore1());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore2(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 2 " + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore2());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore3(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 3" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore3());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore4(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 4" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore4());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore5(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 5" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore5());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String blockCore6(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("Block core 6" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + bps.get(loadPoint).get(replic).getNumBlockCore6());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
}

package grmlsa.csa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import grmlsa.Route;
import network.Circuit;
import network.ControlPlane;
import network.Crosstalk;
import network.Link;
import network.Mesh;
import network.PhysicalLayer;
import util.IntersectionFreeSpectrum;

// Algoritmo proposto em: "Inter-core crosstalk aware greedy algorithm for spectrum and core assignment in space division multiplexed elastic optical networks" 
// Autores: Fabricio R.L. Lobato, Antonio Jacob, Jhonatan Rodrigues, Adolfo V.T. Cartaxo, J.C.W.A. Costa.
// Revista: Optical Switching and Networking
// Ano: 2019
//

public class XtAwareGreedyAlgorithm implements CoreAndSpectrumAssignmentAlgorithmInterface{
	private int coreOfTheTime;
	private double deltaXtCandidate;
	private double deltaXtBestCandidate;
	private Circuit circuitCandidate;
	private Circuit circuitBestCandidate;
	private Crosstalk crosstalk;
		
	public XtAwareGreedyAlgorithm() {
		this.crosstalk = new Crosstalk();
	}
	
	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		this.circuitCandidate = new Circuit();
		this.circuitBestCandidate = new Circuit();
		this.deltaXtCandidate = 0.0;
		this.deltaXtBestCandidate = -999999999.0; //The smallest value
		this.coreOfTheTime = 0;
					
		circuit.setSpectrumAssigned(policy(numberOfSlots, null, circuit, cp));
        circuit.setIndexCore(circuitBestCandidate.getIndexCore());
        
        if (circuit.getSpectrumAssigned() == null) {
 	       return false;
 		}
        

        return true;

	}
	
	

	@Override
	public int coreAssignment() {
		int temp = this.coreOfTheTime;
		this.coreOfTheTime++;
		return temp;
	}

	
	
	
	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		this.circuitCandidate = new Circuit();
		this.circuitBestCandidate = new Circuit();
		
		while (coreOfTheTime < cp.getMesh().getLinkList().get(0).NUMBEROFCORES) {
			List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), coreOfTheTime);
			
			int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
	        if(numberOfSlots> maxAmplitude) return null;
			
			for (int[] band : composition) {
		    	
				for (int i = band[0]; i <= band[1]; i++) {
					int chosen[] = null;
		            if (band[1] - i + 1 >= numberOfSlots) {
		                chosen = band.clone();
		                chosen[0] = i;
		                chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
		                
		                //circuitCandidate = circuit;		                
		                
		                circuitCandidate.setRoute(circuit.getRoute());
		        		circuitCandidate.setModulation(circuit.getModulation());
		        		circuitCandidate.setGuardBand(circuit.getGuardBand());
		                
		                circuitCandidate.setSpectrumAssigned(chosen);
		                circuitCandidate.setIndexCore(coreOfTheTime);
		                circuitCandidate.setXt(crosstalk.calculaCrosstalk(circuitCandidate));
		                
		                circuitCandidate.setQoT(true);
		                circuitCandidate.setQoTForOther(true);
		                circuitCandidate.setSNR(999999.9);
		                
		                
		                if(isAdmissibleQualityOfTransmission(circuitCandidate, cp.getMesh())) { //QOTN QOTO
			                if (circuitCandidate.getXt() != -120) {
			                	if(circuitCandidate.getXt() <= crosstalk.xtThreshold(circuitCandidate.getModulation())) {//XTN 
			                		if(crosstalk.isAdmissibleInOthers(circuitCandidate)) {
			                			
				                			deltaXtCandidate = cp.getMesh().getCrosstalk().xtThreshold(circuitCandidate.getModulation()) - circuitCandidate.getXt();
				                			if(deltaXtCandidate > deltaXtBestCandidate) {
				                				circuitBestCandidate.setSpectrumAssigned(circuitCandidate.getSpectrumAssigned());
				                				circuitBestCandidate.setIndexCore(coreOfTheTime);
				                				deltaXtBestCandidate = deltaXtCandidate;		                				
				                			}
			                			
			                		}
			                	}
			                }else {
			                	circuitBestCandidate.setSpectrumAssigned(circuitCandidate.getSpectrumAssigned());
			                	circuitBestCandidate.setIndexCore(coreOfTheTime);
			                	return circuitBestCandidate.getSpectrumAssigned();		                	 
			                }
		               }
		            }
				}	            
	        }
			
			coreAssignment();
		}	
		
		return circuitBestCandidate.getSpectrumAssigned();
	}
		
	
//	private boolean xtNeighborIsAdmissible(Circuit circuitCandidate) {
//		ArrayList<Core> adjacentsCores = circuitCandidate.getRoute().getLink(0).coresAdjacents(circuitCandidate.getIndexCore());
//		
//		for(Core neighbor : adjacentsCores) {
//			Circuit circuitJ = new Circuit();
//			circuitJ.setIndexCore(neighbor.getId());
//			circuitJ.setRoute(circuitCandidate.getRoute());
//			circuitJ.sets
//					
//		}
//		
//		return true;
//	}
	
	//
	// Adicionado o metodo de verificar QoTN e QoTo. Nâo é original do artigo, mas foi adicionado para prover uma comparação justa
	//
	
protected boolean isAdmissibleQualityOfTransmission(Circuit circuit, Mesh mesh) {
    	
    	// Check if it is to test the QoT
    	//if(mesh.getPhysicalLayer().isActiveQoT()){
    		
    		// Verifies the QoT of the current circuit
    		if(computeQualityOfTransmission(circuit, null, false, mesh)){
    			boolean QoTForOther = true;
    			
    			// Check if it is to test the QoT of other already active circuits
    			//if(mesh.getPhysicalLayer().isActiveQoTForOther()){
    				
    				// Calculates the QoT of the other circuits
    			QoTForOther = computeQoTForOther(circuit, mesh);
    			circuit.setQoTForOther(QoTForOther);
    			//}
    			
    			return QoTForOther;
    		}
    		
    		return false; // Circuit can not be established
    	//}
    	
		// If it does not check the QoT then it returns acceptable
		//return true;
    }
		
	
    
    public boolean computeQualityOfTransmission(Circuit circuit, Circuit testCircuit, boolean addTestCircuit, Mesh mesh){
    	//double sigma = 1.0;
    	double SNR = mesh.getPhysicalLayer().computeSNRSegment(circuit, circuit.getRoute(), 0, circuit.getRoute().getNodeList().size() - 1, circuit.getModulation(), circuit.getSpectrumAssigned(), testCircuit, addTestCircuit);
		double SNRdB = PhysicalLayer.ratioForDB(SNR);
		circuit.setSNR(SNRdB);
		
		boolean QoT = mesh.getPhysicalLayer().isAdmissible(circuit.getModulation(), SNRdB, SNR);
		circuit.setQoT(QoT);
		
		//System.out.println("--------\n A OSNR é: "+SNRdB+"\n\n----------");
		
		return QoT;
    }
    
    public boolean computeQoTForOther(Circuit circuit, Mesh mesh){
    	HashSet<Circuit> circuits = new HashSet<Circuit>(); // Circuit list for test
    	HashMap<Circuit, Double> circuitsSNR = new HashMap<Circuit, Double>(); // To guard the SNR of the test list circuits
    	HashMap<Circuit, Boolean> circuitsQoT = new HashMap<Circuit, Boolean>(); // To guard the QoT of the test list circuits
		
    	// Search for all circuits that have links in common with the circuit under evaluation
		Route route = circuit.getRoute();
		for (Link link : route.getLinkList()) {
			
			// Picks up the active circuits that use the link
			HashSet<Circuit> circuitsTemp = link.getCore(circuit.getIndexCore()).getCircuitList();
            for (Circuit circuitTemp : circuitsTemp) {
            	
            	// If the circuit is different from the circuit under evaluation and is not in the circuit list for test
                if (!circuit.equals(circuitTemp) && !circuits.contains(circuitTemp)) {
                    circuits.add(circuitTemp);
                }
            }
		}
		
		// Tests the QoT of circuits
        for (Circuit circuitTemp : circuits) {
        	
        	// Stores the SNR and QoT values
        	circuitsSNR.put(circuitTemp, circuitTemp.getSNR());
            circuitsQoT.put(circuitTemp, circuitTemp.isQoT());
            
        	// Recalculates the QoT and SNR of the circuit
            //boolean QoT = computeQualityOfTransmission(circuitTemp, null, true, mesh);
            
            boolean QoT = computeQualityOfTransmission(circuitTemp, circuit, true, mesh);
            
            // Returns the SNR and QoT values of circuits before the establishment of the circuit in evaluation
        	//for (Circuit circuitAux : circuitsSNR.keySet()) {
        		//circuitAux.setSNR(circuitsSNR.get(circuitAux));
        		//circuitAux.setQoT(circuitsQoT.get(circuitAux));
        	//}
            
            if (!QoT) {
            	
            	// Returns the SNR and QoT values of circuits before the establishment of the circuit in evaluation
            	for (Circuit circuitAux : circuitsSNR.keySet()) {
            		circuitAux.setSNR(circuitsSNR.get(circuitAux));
            		circuitAux.setQoT(circuitsQoT.get(circuitAux));
            	}
            	
                return false;
            }
            
            
        }
        
        // Returns the SNR and QoT values of circuits before the establishment of the circuit in evaluation
    	for (Circuit circuitAux : circuitsSNR.keySet()) {
    		circuitAux.setSNR(circuitsSNR.get(circuitAux));
    		circuitAux.setQoT(circuitsQoT.get(circuitAux));
    	}
        
		return true;
    }

}

package grmlsa.csa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import grmlsa.Route;
import network.Circuit;
import network.ControlPlane;
import network.Crosstalk;
import network.Link;
import network.Mesh;
import network.PhysicalLayer;
import util.IntersectionFreeSpectrum;

// Algoritmo ICXT-Aware, proposto em LIU 2020.
// Adaptação da estratégia XT aware de LIU 2020.
// Artigo: Routing Core and Spectrum Allocation Algorithm for Inter-Core Crosstalk and Energy Efficiency in Space Division Multiplexing Elastic Optical Networks
// A IMPLEMENTAÇÃO A SEGUIR É VÁLIDA APENAS PARA UMA FIBRA DE 7 NÚCLEOS E 320 SLOTS EM CADA NÚCLEO.

public class IcxtAwareAlgorithm implements CoreAndSpectrumAssignmentAlgorithmInterface{
	private int si; //slot inicial
	private int f; //total de slots no núcleo
	private int tc; //total de núcleos
	private int gi; // total de núcleos no grupo i
	private int eg1[]; //Espectro prioritário do grupo 1
	private int eg2[]; //Espectro prioritário do grupo 2
	private int eg3[]; //Espectro prioritário do grupo 3
	private List<Integer> g1; // Núcleos que fazem parte do grupo 1
	private List<Integer> g2; // Núcleos que fazem parte do grupo 2
	private List<Integer> g3; // Núcleos que fazem parte do grupo 3
	private Crosstalk crosstalk;
	private Circuit circuitoCandidato;
	
	public IcxtAwareAlgorithm() {
		this.crosstalk = new Crosstalk();
		this.circuitoCandidato = new Circuit();
		
		this.si = 1;
		this.f = 320;
		this.tc = 7;
		
		this.eg1 = new int[2];
		this.eg2 = new int[2];
		this.eg3 = new int[2];
		
		this.eg1[0] = 1;
		this.eg1[1] = 137;
		
		this.eg2[0] = 138;
		this.eg2[1] = 274;
		
		this.eg3[0] = 275;
		this.eg3[1] = 320;
		
		this.g1 = new ArrayList<>();
		this.g1.add(1);
		this.g1.add(3);
		this.g1.add(5);
		
		this.g2 = new ArrayList<>();
		this.g2.add(2);
		this.g2.add(4);
		this.g2.add(6);
		
		this.g3 = new ArrayList<>();
		this.g3.add(0);

	}
	
	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		this.circuitoCandidato.setRoute(circuit.getRoute());
		this.circuitoCandidato.setModulation(circuit.getModulation());
		this.circuitoCandidato.setGuardBand(circuit.getGuardBand());

		//this.circuitoCandidato.setIndexCore(5);
		//System.out.println(circuit.getIndexCore()+"---"+this.circuitoCandidato.getIndexCore());
		
		int chosenCore = 6;
		int chosen[] = null;
		//int contadorF = numberOfSlots;
		
		
		
		//Primeira tentativa
		for(int c=6; c>=0; c--) {
			List<int[]> compositionantiga = IntersectionFreeSpectrum.merge(circuitoCandidato.getRoute(), circuitoCandidato.getGuardBand(), c);
			
			if(g1.contains(c)) {
	    		chosen = alocacaoGrupo1Xt(numberOfSlots, compositionantiga, circuitoCandidato, cp, c);
	    	}
	    	
	    	if(g2.contains(c)) {
	    		chosen = alocacaoGrupo2Xt(numberOfSlots, compositionantiga, circuitoCandidato, cp, c);
	    	}
	    	
	    	if(g3.contains(c)) {
	    		chosen = alocacaoGrupo3Xt(numberOfSlots, compositionantiga, circuitoCandidato, cp, c);
	    	}
	    	
	    	if(chosen!=null) {
	    		circuitoCandidato.setIndexCore(c);
	    		circuitoCandidato.setSpectrumAssigned(chosen);
	    		
	    		if(crosstalk.isAdmissible(circuitoCandidato) && crosstalk.isAdmissibleInOthers(circuitoCandidato)) {
	    			circuit.setSpectrumAssigned(chosen);
	    	        circuit.setIndexCore(c);
	    	        return true;
	    		}
	    	}
		}
		
		//Segunda tentativa
		/*for(int c=6; c>=0; c--) {
			List<int[]> compositionantiga2 = IntersectionFreeSpectrum.merge(circuitoCandidato.getRoute(), circuitoCandidato.getGuardBand(), c);
			
			if(g1.contains(c)) {
	    		chosen = alocacaoGrupo1(numberOfSlots, compositionantiga2, circuit, cp);
	    	}
	    	
	    	if(g2.contains(c)) {
	    		chosen = alocacaoGrupo2(numberOfSlots, compositionantiga2, circuit, cp);
	    	}
	    	
	    	if(g3.contains(c)) {
	    		chosen = alocacaoGrupo3(numberOfSlots, compositionantiga2, circuit, cp);
	    	}
	    	
	    	if(chosen!=null) {
	    		circuit.setSpectrumAssigned(chosen);
	    	    circuit.setIndexCore(c);
	    	    return true;
	    		
	    	}
	
		}*/
		   	
    	
		return false;
    	
 
	}

	@Override
	public int coreAssignment() {
		Random generator = new Random();
		int a = generator.nextInt(this.tc);
		return a;
	}

	
	//Firt fit
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
        for (int[] band : freeSpectrumBands) {
        	
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band.clone();
                chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                break;
            }
        }
        
        return chosen;
	}
	
	//Firt fit
	public int[] policyXt(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp, int core) {
		int maxAmplitude = 80;
	    if(numberOfSlots> maxAmplitude) return null;
	    int chosen[] = null;
	    	
	    for (int[] band : freeSpectrumBands) {
	    	
	    	for (int i = band[0]; i <= band[1]; i++) {
				//int chosen2[] = null;
	            
				if (band[1] - i + 1 >= numberOfSlots) {
	                chosen = band.clone();
	                chosen[0] = i;
	                chosen[1] = chosen[0] + numberOfSlots - 1;
	                
	                if(chosen!=null) {
	    	    		circuitoCandidato.setIndexCore(core);
	    	    		circuitoCandidato.setSpectrumAssigned(chosen);
	    	    		
	    	    		circuitoCandidato.setQoT(true);
			            circuitoCandidato.setQoTForOther(true);
			            circuitoCandidato.setSNR(999999.9);
	    	    		
	    	    		if(isAdmissibleQualityOfTransmission(circuitoCandidato, cp.getMesh())) { //QOTN QOTO
		    	    		if(crosstalk.isAdmissible(circuitoCandidato) && crosstalk.isAdmissibleInOthers(circuitoCandidato)) {
		    	    			//circuit.setSpectrumAssigned(chosen);
		    	    	        //circuit.setIndexCore(core);
		    	    	        return chosen;
		    	    		}
	    	    		}
	    	    	}
	            }
	        }
	     }
	        
	    return null;
	}
	
	public int[] alocacaoGrupo1(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
    	List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition1, circuit, cp);
        if (chosen!=null){return chosen;}
        
        //Grupo de prioridade 2
        List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition2, circuit, cp);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor propridade
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition3, circuit, cp);
        if (chosen!=null){return chosen;}
                
        return chosen;
	}
	
	public int[] alocacaoGrupo2(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
    	List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition2, circuit, cp);
        if (chosen!=null){return chosen;}
        
        //Grupo de prioridade 2
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition3, circuit, cp);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor propridade
        List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition1, circuit, cp);
        if (chosen!=null){return chosen;}
                
        return chosen;
	}
	
	
	public int[] alocacaoGrupo3(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition3, circuit, cp);
        if (chosen!=null){return chosen;}
    	
        //Grupo de prioridade 2
    	List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition1, circuit, cp);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor prioridade
        List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policy(numberOfSlots, composition2, circuit, cp);
        if (chosen!=null){return chosen;}
        

                
        return chosen;
	}
	
	public int[] alocacaoGrupo1Xt(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp, int c) {
		int maxAmplitude = 80;
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
    	List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition1, circuit, cp, c);
        if (chosen!=null){return chosen;}
        
        //Grupo de prioridade 2
        List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition2, circuit, cp, c);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor propridade
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition3, circuit, cp, c);
        if (chosen!=null){return chosen;}
                
        return chosen;
	}
	
	public int[] alocacaoGrupo2Xt(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp, int c) {
		int maxAmplitude = 80;
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
    	List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition2, circuit, cp, c);
        if (chosen!=null){return chosen;}
        
        //Grupo de prioridade 2
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition3, circuit, cp, c);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor propridade
        List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition1, circuit, cp, c);
        if (chosen!=null){return chosen;}
                
        return chosen;
	}
	
	
	public int[] alocacaoGrupo3Xt(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp, int c) {
		int maxAmplitude = 80;
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	//Grupo prioritário
        List<int[]> composition3 = novaComposicao3(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition3, circuit, cp, c);
        if (chosen!=null){return chosen;}
    	
        //Grupo de prioridade 2
    	List<int[]> composition1 = novaComposicao1(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition1, circuit, cp, c);
        if (chosen!=null){return chosen;}
        
        //Grupo de menor prioridade
        List<int[]> composition2 = novaComposicao2(freeSpectrumBands, circuit, cp);    	
    	chosen = policyXt(numberOfSlots, composition2, circuit, cp, c);
        if (chosen!=null){return chosen;}
        

                
        return chosen;
	}
	

	
	private List<int[]> novaComposicao1(List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		List<int[]> novacomposition = new ArrayList<>();
		

		
		for(int[] band : freeSpectrumBands) {
			// Está completamente dentro do intervalo
			if ((band[1] >= this.eg1[0]) && (band[1] <= this.eg1[1]) && (band[0] >= this.eg1[0]) && (band[0] <= this.eg1[1])) {
				novacomposition.add(band.clone());
            }
			
			// Está parcialmente no limite inferior
			if ((band[1] >= this.eg1[0]) && (band[1] <= this.eg1[1]) && (band[0] < this.eg1[0]) && (band[0] < this.eg1[1])) {
				int[] novo = new int[2];
				novo[0] = this.eg1[0];
				novo[1] = band[1];
				novacomposition.add(novo);
            }
			
			// Está parcialmente no limite supeior
			if ((band[1] > this.eg1[1]) && (band[1] > this.eg1[0]) && (band[0] <= this.eg1[1]) && (band[0] > this.eg1[0])) {
				int[] novo = new int[2];
				novo[0] = band[0];
				novo[1] = this.eg1[1];
				novacomposition.add(novo);
            }
			
			// Engloba os limites
			if ((band[1] > this.eg1[1]) && (band[1] > this.eg1[0]) && (band[0] < this.eg1[1]) && (band[0] < this.eg1[0])) {
				int[] novo = new int[2];
				novo[0] = this.eg1[0];
				novo[1] = this.eg1[1];
				novacomposition.add(novo);
			}
					
		}
		return novacomposition;
	}
	
	private List<int[]> novaComposicao2(List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		List<int[]> novacomposition = new ArrayList<>();
		

		
		for(int[] band : freeSpectrumBands) {
			// Está completamente dentro do intervalo
			if ((band[1] >= this.eg2[0]) && (band[1] <= this.eg2[1]) && (band[0] >= this.eg2[0]) && (band[0] <= this.eg2[1])) {
				novacomposition.add(band.clone());
            }
			
			// Está parcialmente no limite inferior
			if ((band[1] >= this.eg2[0]) && (band[1] <= this.eg2[1]) && (band[0] < this.eg2[0]) && (band[0] < this.eg2[1])) {
				int[] novo = new int[2];
				novo[0] = this.eg2[0];
				novo[1] = band[1];
				novacomposition.add(novo);
            }
			
			// Está parcialmente no limite supeior
			if ((band[1] > this.eg2[1]) && (band[1] > this.eg2[0]) && (band[0] <= this.eg2[1]) && (band[0] > this.eg2[0])) {
				int[] novo = new int[2];
				novo[0] = band[0];
				novo[1] = this.eg2[1];
				novacomposition.add(novo);
            }
			
			// Engloba os limites
			if ((band[1] > this.eg2[1]) && (band[1] > this.eg2[0]) && (band[0] < this.eg2[1]) && (band[0] < this.eg2[0])) {
				int[] novo = new int[2];
				novo[0] = this.eg2[0];
				novo[1] = this.eg2[1];
				novacomposition.add(novo);
			}
					
		}
		return novacomposition;
	}
	
	private List<int[]> novaComposicao3(List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		List<int[]> novacomposition = new ArrayList<>();
		

		
		for(int[] band : freeSpectrumBands) {
			// Está completamente dentro do intervalo
			if ((band[1] >= this.eg3[0]) && (band[1] <= this.eg3[1]) && (band[0] >= this.eg3[0]) && (band[0] <= this.eg3[1])) {
				novacomposition.add(band.clone());
            }
			
			// Está parcialmente no limite inferior
			if ((band[1] >= this.eg3[0]) && (band[1] <= this.eg3[1]) && (band[0] < this.eg3[0]) && (band[0] < this.eg3[1])) {
				int[] novo = new int[2];
				novo[0] = this.eg3[0];
				novo[1] = band[1];
				novacomposition.add(novo);
            }
			
			// Está parcialmente no limite supeior
			if ((band[1] > this.eg3[1]) && (band[1] > this.eg3[0]) && (band[0] <= this.eg3[1]) && (band[0] > this.eg3[0])) {
				int[] novo = new int[2];
				novo[0] = band[0];
				novo[1] = this.eg3[1];
				novacomposition.add(novo);
            }
			
			// Engloba os limites
			if ((band[1] > this.eg3[1]) && (band[1] > this.eg3[0]) && (band[0] < this.eg3[1]) && (band[0] < this.eg3[0])) {
				int[] novo = new int[2];
				novo[0] = this.eg3[0];
				novo[1] = this.eg3[1];
				novacomposition.add(novo);
			}
					
		}
		return novacomposition;
	}








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


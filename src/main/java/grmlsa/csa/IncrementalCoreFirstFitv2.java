package grmlsa.csa;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

public class IncrementalCoreFirstFitv2 implements CoreAndSpectrumAssignmentAlgorithmInterface{
	public static final int QUANTCENTRALCORE = 100;
	
	private int coreOfTheTime;
	private int contCentralCore;
	
	public IncrementalCoreFirstFitv2() {
		this.coreOfTheTime = 0;
		this.contCentralCore = QUANTCENTRALCORE;
		
	}
	
	
	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		int chosenCore = coreAssignment();
		
		//System.out.println("O n�cleo escolhido � o: "+chosenCore);
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), chosenCore);
        
        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        circuit.setIndexCore(chosenCore);
        
       // System.out.println("teste1");
        
       // teste(circuit, chosen, chosenCore);
        
        if (chosen == null)
        	return false;

        return true;
	}

	@Override
	public int coreAssignment() {
		if ((coreOfTheTime == 0)) {
			if (contCentralCore == 0) {
				contCentralCore = QUANTCENTRALCORE;
				coreOfTheTime++;
				return 0;
			}else {
				contCentralCore--;
				coreOfTheTime++;
			}
			
		}
		
		if (coreOfTheTime == 6) {
    		coreOfTheTime = 0;
    		return 6;
    	}else {
    		int temp = coreOfTheTime;
    		coreOfTheTime++;
    		return temp;
    	}
	}


	@Override
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
        
       // System.out.println("teste2");
        
        return chosen;
	}

}
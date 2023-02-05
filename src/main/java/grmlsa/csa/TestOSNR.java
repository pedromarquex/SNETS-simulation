package grmlsa.csa;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

public class TestOSNR implements CoreAndSpectrumAssignmentAlgorithmInterface{

	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		int chosenCore = coreAssignment();
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), chosenCore);
        
        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        circuit.setIndexCore(chosenCore);
        
       // System.out.println("teste1");
        
       // teste(circuit, chosen, chosenCore);
        
       // testes(circuit);
        
        if (chosen == null)
        	return false;

        return true;
	}

	@Override
	public int coreAssignment() {
		
		return 1;
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
	
	private void testes(Circuit circuit) {
		System.out.println("----------------------------------------\nTestes de Camada Física:");
		
		System.out.println(" O núcleo usado é: "+circuit.getIndexCore());
		System.out.println(" A rota é: "+circuit.getRoute().getSource().getName()+"-"+circuit.getRoute().getDestination().getName());
		System.out.println(" O tamanho da rota é: "+circuit.getRoute().getDistanceAllLinks()+" km");
		System.out.println(" A modulação usada é: "+circuit.getModulation().getName());
		System.out.println(" O OSNR do circuito é: "+circuit.getSNR());
		
		System.out.println("----------------------------------------\n");
		
		
	}

}

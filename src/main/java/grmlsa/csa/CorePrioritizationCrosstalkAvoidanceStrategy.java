package grmlsa.csa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import network.Circuit;
import network.ControlPlane;
import network.Core;
import network.Link;
import util.IntersectionFreeSpectrum;

// Algoritmo CAS, proposto em LIU 2020.
// Adaptação da estratégia XT avoid de LIU 2020.
// Artigo: Routing Core and Spectrum Allocation Algorithm for Inter-Core Crosstalk and Energy Efficiency in Space Division Multiplexing Elastic Optical Networks
// A IMPLEMENTAÇÃO A SEGUIR É VÁLIDA APENAS PARA UMA FIBRA DE 7 NÚCLEOS E 320 SLOTS EM CADA NÚCLEO.
public class CorePrioritizationCrosstalkAvoidanceStrategy implements CoreAndSpectrumAssignmentAlgorithmInterface{
	private static final int MAXPESOCORE = 99999;
	private int si; //slot inicial
	private int f; //total de slots no núcleo
	private int c; //total de núcleos
	private int gi; // total de núcleos no grupo i
	private int eg1[]; //Espectro prioritário do grupo 1
	private int eg2[]; //Espectro prioritário do grupo 2
	private int eg3[]; //Espectro prioritário do grupo 3
	private List<Integer> g1; // Núcleos que fazem parte do grupo 1
	private List<Integer> g2; // Núcleos que fazem parte do grupo 2
	private List<Integer> g3; // Núcleos que fazem parte do grupo 3
	
	public CorePrioritizationCrosstalkAvoidanceStrategy() {
		this.si = 1;
		this.f = 320;
		this.c = 7;
		
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
		int chosenCore = coreAssignment(circuit);
		int chosen[] = null;
		//int contadorF = numberOfSlots;
		
    	List<int[]> compositionantiga = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), chosenCore);
    	
    	if(g1.contains(chosenCore)) {
    		chosen = alocacaoGrupo1(numberOfSlots, compositionantiga, circuit, cp);
    	}
    	
    	if(g2.contains(chosenCore)) {
    		chosen = alocacaoGrupo2(numberOfSlots, compositionantiga, circuit, cp);
    	}
    	
    	if(g3.contains(chosenCore)) {
    		chosen = alocacaoGrupo3(numberOfSlots, compositionantiga, circuit, cp);
    	}
    	
    	circuit.setSpectrumAssigned(chosen);
        circuit.setIndexCore(chosenCore);
         

        if (chosen == null) {
        	return false;
        }
        
        return true;
    	
 
	}

	@Override
	public int coreAssignment() {
		Random generator = new Random();
		int a = generator.nextInt(this.c);
		return a;
	}
	
	public int coreAssignment(Circuit circuit) {
		int coreEscolhido = 0;
		int pesoCoreEscolhido = MAXPESOCORE * circuit.getRoute().getLinkList().size();
		
		// Vendo qual core tem o menor peso total
		for(int i=6; i>=0; i--) {
			int pesoCore = 0;
			
			for(Link link : circuit.getRoute().getLinkList()) {
				pesoCore = pesoCore + link.getCore(i).getPeso();
			}
			
			if(pesoCore < pesoCoreEscolhido) {
				coreEscolhido = i;
				pesoCoreEscolhido = pesoCore;
			}
		}
		
		//Atualizando os pesos
		for(Link link : circuit.getRoute().getLinkList()) {
			link.getCore(coreEscolhido).setPeso(MAXPESOCORE);
			link.atualizaPesosCores(coreEscolhido);
		}
		
		
		//zera os pesos
		for(Link link : circuit.getRoute().getLinkList()) {
			boolean flag = true;
			
			for(Core core : link.getCores()) {
				if(core.getPeso() < MAXPESOCORE) {
					flag = false;
					break;
				}
			}
			
			if(flag) {
				//System.out.println("Peso resetado no link "+link.getName());
				link.renovaTodosOsPesos();
			}
		}
		
		//Zera os pesos
		//if(pesoCoreEscolhido >= (MAXPESOCORE * circuit.getRoute().getLinkList().size() ) ){
			//for(Link link : circuit.getRoute().getLinkList()) {
				//link.renovaTodosOsPesos();
			//}
		//}
		

		return coreEscolhido;
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

}

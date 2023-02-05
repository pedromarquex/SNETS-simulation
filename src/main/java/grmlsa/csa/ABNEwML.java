package grmlsa.csa;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import network.Circuit;
import network.ControlPlane;
import network.Core;
import network.Link;
import network.Spectrum;
import util.IntersectionFreeSpectrum;

//CBA-SBA
public class ABNEwML implements CoreAndSpectrumAssignmentAlgorithmInterface{
	public static final int BREAKPOINT1 = 107;
	public static final int BREAKPOINT2 = 214;
	
	public static final int WEIGHT1 = 6;
	public static final int WEIGHT2 = 3;
	public static final int WEIGHT3 = 1;
	public static final int WEIGHT4 = 2;
	
	public static final int WEIGHTABS = 1;
	public static final int WEIGHTWEI = 1;
	public static final int WEIGHTROUT = 8;
	
	private int coreOfTheTime;
	private int numberOfSlots;
	private int spectrumUtilizationAbsolut[];
	private int ultimoCore;
	
	public ABNEwML() {
		this.coreOfTheTime = 0;
		this.numberOfSlots = 7; //indices de slots
		this.spectrumUtilizationAbsolut = new int[numberOfSlots];
	}
	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		spectrumUtilizationAbsolute(circuit, cp);
		spectrumUtilizationWeighted(circuit, cp);
		spectrumUtilizationInRoute(circuit, cp);
		
		int chosenCore = coreAssignment(circuit);
		int chosen[] = null;
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), chosenCore);
        
    	//System.out.println("\n\n Testes");
    	
    	if(chosenCore == 0) {
    		//System.out.println("Medium Fit escolhido");
    		chosen = policy(numberOfSlots, composition, circuit, cp);
    	}else if(chosenCore%2 == 0) {
    		//System.out.println("Last Fit escolhido");
    		chosen = policy1(numberOfSlots, composition, circuit, cp);
    	}else if(chosenCore%2 == 1) {
    		//System.out.println("First Fit escolhido");
    		chosen = policy2(numberOfSlots, composition, circuit, cp);
    		
    	}
    	
 
    	//System.out.println("N�cleo: "+chosenCore);
    	//System.out.println("spectrum: "+chosen[0]+"-"+chosen[1]);
        circuit.setSpectrumAssigned(chosen);
        circuit.setIndexCore(chosenCore);
        
        
        if (chosen == null) {
        	return false;
        }

        	
        return true;
	}
	
	public int coreAssignment() {
		if (coreOfTheTime == 6) {
    		coreOfTheTime = 0;
    		return 6;
    	}else {
    		int temp = coreOfTheTime;
    		coreOfTheTime++;
    		return temp;
    	}
	}
	
	public int coreAssignment(Circuit circuit) {
		int core = 6;
		int coreVector[] = new int[7];
		
		for (int i = 6; i>=0; i--) {
			coreVector[i] = (circuit.getSpectrumUtilizationAbsolut()[i] * WEIGHTABS) + (circuit.getSpectrumUtilizationWeighted()[i] * WEIGHTWEI) + (circuit.getSpectrumUtilizationInRoute()[i] * WEIGHTROUT);
		}
		
		for (int i = 6; i>=0; i--) {
			if ((coreVector[i] < coreVector[core])) {
				core = i;
			}
		}
		
		return core;
    	
	}

	//
	// Machine Learning
	// Machine Learning
	// Machine Learning
	// Machine Learning
	// Machine Learning
	//
	/*	@Override
	public int coreAssignment() {
		String mensagem = "";
		link = linkMostUsed(route);
		//para MLP
		//mensagem = link.getUsedSlots() + "/" + link.getUtilization() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + numberOfFreeSlots(route) + "/" + UtilizacaoGeral(cp.getMesh()) + "/" + mod.getM() + "/" + circuit.getSNR();
		//para CONV
		mensagem = link.getUsedSlots() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + mod.getM();
		//System.out.println(mensagem);
		//mensagem = link.getUtilization() +"/"+ mod.getM();
		// Conectando com o servidor
		Socket cliente = null;
		try {
			cliente = new Socket("127.0.0.1", 7000);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Enviando dados para o servidor
		PrintStream saida = null;
		try {
			saida = new PrintStream(cliente.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		saida.println(mensagem);

		//Pegando retorno do servidor
		Scanner s = null;
		try {
			s = new Scanner(cliente.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		guardBand = Integer.parseInt(s.nextLine());
		//System.out.println(mensagem);
		//System.out.println(guardBand);
		
		// Fechando a conex�o
		s.close();
		saida.close();
		try {
			cliente.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

*/

	//"medium fit
	//policy for central core
	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		int reference = circuit.getRoute().getLink(0).getCore(0).getNumOfSlots()/2;
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;
    	int chosen[] = null;
    	
    	for (int[] band : freeSpectrumBands) {
        	if(chosen == null) {
        		if (band[1] - band[0] + 1 >= numberOfSlots) {
                    chosen = band.clone();
                    chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                    break;
                }       		
        	}
    	}
    	
    	
    	if(chosen != null) {
	    	for (int[] band : freeSpectrumBands) {	
	        	//if(chosen != null) {
	        	
		        	for(int i = band[0]; i<=band[1]; i++) {
		        		if(Math.abs(reference-i) < Math.abs(reference-chosen[0])) {
		        			if((band[1]-i+1) >= numberOfSlots) {
		        				chosen[0] = i;
		        				chosen[1] = i + numberOfSlots - 1;
		        			}
		        		}
		        		
		//        		if(i<reference) {
		//        			if((i+reference)>(chosen[0]+reference)) {
		//            			if((band[1]-i+1) >= numberOfSlots) {
		//            				chosen[0] = i;
		//            				chosen[1] = i + numberOfSlots - 1;
		//            			}
		//            		}
		//        		}else {
		//        			if((i+reference)<(chosen[0]+reference)) {
		//            			if((band[1]-i+1) >= numberOfSlots) {
		//            				chosen[0] = i;
		//            				chosen[1] = i + numberOfSlots - 1;
		//            			}
		//            		}
		//        		}
		        	}	
		        		
		        		
		      }
        	
    	}
        
    	
        
        return chosen;
	}
	
	//Last Fit
	//policy for 2,4,6 core
	public int[] policy1(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		 int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
	        if(numberOfSlots>maxAmplitude) return null;
	    	int chosen[] = null;
	        int band[] = null;
	        
	        for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
	            band = freeSpectrumBands.get(i);
	            
	            if (band[1] - band[0] + 1 >= numberOfSlots) {
	                chosen = band.clone();
	                chosen[0] = chosen[1] - numberOfSlots + 1;//It is not necessary to allocate the entire band, just the amount of slots required
	                break;
	            }
	        }

	        return chosen;
	}
	
	//First Fit
	//policy for 1,3,5 core
	public int[] policy2(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
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
	
	
	
	// Spectrum Ultilization in Route
	private void spectrumUtilizationInRoute (Circuit circuit, ControlPlane cp) {
		int contador = 0;
		Vector<Link> listaDeLinks;
		listaDeLinks = circuit.getRoute().getLinkList();
		
		for (Link link : listaDeLinks) {
			for (int i = 0; i<7; i++) {
				circuit.refreshSpectrumUtilizationInRouteUnit(i, circuit.getSpectrumUtilizationInRoute()[i] + link.getCore(i).getSpectrum().getUsedSlots());
				;
			}
		}
	}
	
	// Spectrum Utilization Absolute
	private void spectrumUtilizationAbsolute (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		ArrayList<Integer> coresAdjacentes;
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		//for (int a = 0; a < this.numberOfSlots; a++) {
		//	circuit.refreshSpectrumUtilizationAbsolutUnit(a, 0);;
		//}
		
		for (int i = 0; i<this.numberOfSlots; i++) {
			//listaDeLinks = null;
			//coresAdjacentes = null;
			
			
			coresAdjacentes = circuit.getRoute().getLink(0).indexOfAdjacentsCores(i);
			
			for (Link link : listaDeLinks) {
				for (Core coreadj : link.getCores()) {
					if (coresAdjacentes.contains(coreadj.getId())) {
						//circuit.refreshSpectrumUtilizationAbsolutUnit(i, (circuit.getSpectrumUtilizationAbsolut()[i] + 1));
						circuit.refreshSpectrumUtilizationAbsolutUnit(i, circuit.getSpectrumUtilizationAbsolut()[i] + coreadj.getUsedSlots());
						//this.spectrumUtilizationAbsolut[i] = this.spectrumUtilizationAbsolut[i] + coreadj.getUsedSlots();
					}
				}				
			}
		}
	}
	
	
	// Spectrum Ultilization Weighted
	private void spectrumUtilizationWeighted (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		ArrayList<Integer> coresAdjacentes;
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		for (int i = 0; i<this.numberOfSlots; i++) {
			
			coresAdjacentes = circuit.getRoute().getLink(0).indexOfAdjacentsCores(i);
			
			for (Link link : listaDeLinks) {
				for (Core coreadj : link.getCores()) {
					if (coresAdjacentes.contains(coreadj.getId())) {
						//circuit.refreshSpectrumUtilizationAbsolutUnit(i, (circuit.getSpectrumUtilizationAbsolut()[i] + 1));
						//circuit.refreshSpectrumUtilizationAbsolutUnit(i, circuit.getSpectrumUtilizationAbsolut()[i] + coreadj.getUsedSlots());
						//this.spectrumUtilizationAbsolut[i] = this.spectrumUtilizationAbsolut[i] + coreadj.getUsedSlots();
						//toApplyWeight(i, sectionSpectrum(spectrum, cp))
						
						circuit.refreshSpectrumUtilizationWeightedUnit(i, circuit.getSpectrumUtilizationWeighted()[i] + toApplyWeight(i, sectionSpectrum(link.getCore(coreadj.getId()).getSpectrum(), cp)));
					}
				}				
			}
		}
	}
	
	private int[] sectionSpectrum (Spectrum spectrum, ControlPlane cp) {
		//int contSpectrum[] = new int[cp.getMesh().getLinkList().get(0).getCore(0).getNumOfSlots()];
		int contSpectrum[] = new int[3];
		List<int[]> freeSpectrum = spectrum.getFreeSpectrumBands();
		boolean usado;
		
		for (int i=1; i<BREAKPOINT1; i++) {
			usado = true;
			
			for (int[] band : freeSpectrum) {
				if (i>=band[0] && i<=band[1]) {			
					usado = false;
					break;
				}				
			}
			
			if(usado) {
				contSpectrum[0]++;
			}
		}
		
		for (int i=BREAKPOINT1; i<BREAKPOINT2; i++) {
			usado = true;
			
			for (int[] band : freeSpectrum) {
				if (i>=band[0] && i<=band[1]) {			
					usado = false;
					break;
				}			
			}
			
			if(usado) {
				contSpectrum[1]++;
			}
		}
		
		for (int i=BREAKPOINT2; i<cp.getMesh().getLinkList().get(0).getCore(0).getNumOfSlots(); i++) {
			usado = true;
			
			for (int[] band : freeSpectrum) {
				if (i>=band[0] && i<=band[1]) {			
					usado = false;
					break;
				}				
			}
			
			if(usado) {
				contSpectrum[2]++;
			}
		}
		
		return contSpectrum;
	}
	
	private int toApplyWeight(int chosenCore, int[] vetor) {
		int totalWeight = 0;
		
		if(chosenCore == 0) {
			totalWeight = (vetor[0]*WEIGHT4) + (vetor[1]*WEIGHT1) + (vetor[2]*WEIGHT4);
    	}else if(chosenCore%2 == 0) {
    		totalWeight = (vetor[0]*WEIGHT3) + (vetor[1]*WEIGHT2) + (vetor[2]*WEIGHT1);
    	}else if(chosenCore%2 == 1) {
    		totalWeight = (vetor[0]*WEIGHT1) + (vetor[1]*WEIGHT2) + (vetor[2]*WEIGHT3);
    	}
		
		return totalWeight;
	}
	
	

}

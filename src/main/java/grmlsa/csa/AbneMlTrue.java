package grmlsa.csa;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import network.Circuit;
import network.ControlPlane;
import network.Core;
import network.Crosstalk;
import network.Link;
import network.Spectrum;
import util.IntersectionFreeSpectrum;

// Algoritmo proposto por Jurandir
// SBRC 2021

public class AbneMlTrue implements CoreAndSpectrumAssignmentAlgorithmInterface{
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
	private double xtCandidate;
	private double xtBestCandidate;
	private Circuit circuitCandidate;
	private Circuit circuitBestCandidate;
	private Crosstalk crosstalk;
	
	private int numberOfCores;
	private int spectrumUtilizationAbsolut[];
	private int ultimoCore;
	
	public static int qc0;
	public static int qc1;
	public static int qc2;
	public static int qc3;
	public static int qc4;
	public static int qc5;
	public static int qc6;
	public static int geral;
	
	private int contCore1;
	
		
	public AbneMlTrue() {
		this.crosstalk = new Crosstalk();
		this.coreOfTheTime = 1;
		this.numberOfCores = 7; //indices de slots
		this.spectrumUtilizationAbsolut = new int[numberOfCores];
		contCore1=1;
	}
	
	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		spectrumUtilizationAbsolute2(circuit, cp);
		spectrumUtilizationWeighted(circuit, cp);
		//calculeAverageXt(circuit, cp);
		int chosen[] = null;
		
		this.circuitCandidate = new Circuit();
		this.circuitBestCandidate = new Circuit();
		this.xtCandidate = 0.0;
		//this.xtBestCandidate = -999999999.0; //The smallest value
		this.xtBestCandidate = 0.0; 
		this.coreOfTheTime = coreAssignment(circuit);
		
		List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), coreOfTheTime);
		
		if(coreOfTheTime == 0) {
    		//System.out.println("Medium Fit escolhido");
    		chosen = policy(numberOfSlots, composition, circuit, cp);
    	}else if(coreOfTheTime%2 == 0) {
    		//System.out.println("Last Fit escolhido");
    		chosen = policy1(numberOfSlots, composition, circuit, cp);
    	}else if(coreOfTheTime%2 == 1) {
    		//System.out.println("First Fit escolhido");
    		chosen = policy2(numberOfSlots, composition, circuit, cp);   		
    	}
					
		circuit.setSpectrumAssigned(chosen);
        circuit.setIndexCore(coreOfTheTime);
        
        if (circuit.getSpectrumAssigned() == null) {
 	       return false;
 		}
        

        return true;

	}
	
	

	@Override
	public int coreAssignment() {
		int temp = this.coreOfTheTime;
		this.coreOfTheTime--;
		return temp;
	}
	
	
	public int coreAssignment(Circuit circuit) {
		int core=5;
		Random randon = new Random();
		//System.out.println("teste1");
		
		String mensagem = "";
		//int[] modML = circuit.getModulationML();
		
		
		//if(contCore1 <= 0) {

			mensagem = circuit.getRoute().getDistanceAllLinks() + "/" + 
					//3 + "/" + 
					circuit.getRoute().getHops() + "/" + //número de saltos
					circuit.getModulation().getM() + "/" +
					circuit.getSpectrumUtilizationAbsolut()[0] + "/" + 
					circuit.getSpectrumUtilizationAbsolut()[1] + "/" +
					circuit.getSpectrumUtilizationAbsolut()[2] + "/" +
					circuit.getSpectrumUtilizationAbsolut()[3] + "/" +
					circuit.getSpectrumUtilizationAbsolut()[4] + "/" +
					circuit.getSpectrumUtilizationAbsolut()[5] + "/" +
					circuit.getSpectrumUtilizationAbsolut()[6] + "/" +
					//circuit.getAverageXt()[0] + "/" + //XT do core 0
					//circuit.getAverageXt()[1] + "/" + //XT do core 0
					//circuit.getAverageXt()[2] + "/" + //XT do core 0
					//circuit.getAverageXt()[3] + "/" + //XT do core 0
					//circuit.getAverageXt()[4] + "/" + //XT do core 0
					//circuit.getAverageXt()[5] + "/" + //XT do core 0
					//circuit.getAverageXt()[6] + "/" + //XT do core 0
					circuit.getSpectrumUtilizationWeighted()[0] + "/" + //SUP do core 0
					circuit.getSpectrumUtilizationWeighted()[1] + "/" + //SUP do core 1
					circuit.getSpectrumUtilizationWeighted()[2] + "/" + //SUP do core 2
					circuit.getSpectrumUtilizationWeighted()[3] + "/" + //SUP do core 3
					circuit.getSpectrumUtilizationWeighted()[4] + "/" + //SUP do core 4
					circuit.getSpectrumUtilizationWeighted()[5] + "/" + //SUP do core 5
					circuit.getSpectrumUtilizationWeighted()[6]; //SUP do core 6
					
			
			//contCore1--;
		
		//}else {
			/*mensagem = circuit.getRoute().getDistanceAllLinks() + "/" + 
					//3 + "/" + 
					1000 + "/" + 
					0 + "/" +
					1000 + "/" +
					0 + "/" +
					0 + "/" +
					0 + "/" +
					1000 + "/" +
					circuit.getAverageXt()[0] + "/" + //XT do core 0
					circuit.getAverageXt()[1] + "/" + //XT do core 0
					circuit.getAverageXt()[2] + "/" + //XT do core 0
					circuit.getAverageXt()[3] + "/" + //XT do core 0
					circuit.getAverageXt()[4] + "/" + //XT do core 0
					circuit.getAverageXt()[5] + "/" + //XT do core 0
					circuit.getAverageXt()[6] + "/" + //XT do core 0
					circuit.getModulation().getM();*/
			
			//contCore1--;
			//return randon.nextInt(6);
		//}

		

		
		//System.out.println(mensagem);
		//System.out.println("Média de XT: "+circuit.getAverageXt()[0]+"-"+circuit.getAverageXt()[1]+"-"+circuit.getAverageXt()[2]+"-"+circuit.getAverageXt()[3]+"-"+circuit.getAverageXt()[4]+"-"+circuit.getAverageXt()[5]+"-"+circuit.getAverageXt()[6]+"-");
		
		Socket cliente = null;
		try {
			cliente = new Socket("127.0.0.1", 7766);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//System.out.println("enviando dados ao servidor");
		// Enviando dados para o servidor
		PrintStream saida = null;
		try {
			saida = new PrintStream(cliente.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		saida.println(mensagem);

		//System.out.println("pegando retorno do servidor");
		//Pegando retorno do servidor
		Scanner s = null;
		try {
			//System.out.println("teste11");
			//System.out.println(s==null);
			//s = new Scanner(cliente.getInputStream());
			s = new Scanner(cliente.getInputStream());
			//System.out.println("S: " + s);
			//System.out.println("teste22");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		String restorno_servidor = s.nextLine();
		restorno_servidor = restorno_servidor.substring(restorno_servidor.length() - 1);
		
		//System.out.println(geral);

		s.close();
		saida.close();
		try {
			cliente.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//System.out.println("teste2");
		core = Integer.parseInt(restorno_servidor);
		
		
		
		//if(core==0) {
			
			//contCore1++;
			//if(contCore1==6) {
			//	contCore1=1;
			//}
			//return contCore1;
			//System.out.println(core);
			//return randon.nextInt(5)+1;
		//}else {
			//System.out.println(core);
			return core+1;
		//}
		//return randon.nextInt(6);
		
	}

	
	
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
	
	// Spectrum Utilization Absolute
	private void spectrumUtilizationAbsolute (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		ArrayList<Integer> coresAdjacentes;
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		//for (int a = 0; a < this.numberOfSlots; a++) {
		//	circuit.refreshSpectrumUtilizationAbsolutUnit(a, 0);;
		//}
		
		for (int i = 0; i<this.numberOfCores; i++) {
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
	
	
	private void spectrumUtilizationAbsolute2 (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		//ArrayList<Integer> coresAdjacentes;
		circuit.refreshSpectrumUtilizationAbsolutUnit(0, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(1, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(2, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(3, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(4, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(5, 0);
		circuit.refreshSpectrumUtilizationAbsolutUnit(6, 0);
		
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		//for (int a = 0; a < this.numberOfSlots; a++) {
		//	circuit.refreshSpectrumUtilizationAbsolutUnit(a, 0);;
		//}
		for(Link link : listaDeLinks) {
			for (int i = 0; i<this.numberOfCores; i++) {
				int quantSlots = 0;
				quantSlots = cp.getMesh().getLink(link.getSource().getName(), link.getDestination().getName()).getCore(i).getSpectrum().getUsedSlots();
				circuit.refreshSpectrumUtilizationAbsolutUnit(i, circuit.getSpectrumUtilizationAbsolut()[i] + quantSlots);
			}
		}
		
	}
	
	
	private void calculeAverageXt (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		//ArrayList<Integer> coresAdjacentes;
		circuit.refreshAverageXt(0, 0);
		circuit.refreshAverageXt(1, 0);
		circuit.refreshAverageXt(2, 0);
		circuit.refreshAverageXt(3, 0);
		circuit.refreshAverageXt(4, 0);
		circuit.refreshAverageXt(5, 0);
		circuit.refreshAverageXt(6, 0);
		
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		
		for (int i = 0; i<this.numberOfCores; i++) {
			double sumXT = 0.0;
			int cont = 0;
			double averageXT = 0.0;
			
			for(Link link : listaDeLinks) {	
				for(Circuit circuitAlocated : link.getCore(i).getCircuitList()) {
					//System.out.println(circuitAlocated.getXt());
					sumXT = sumXT + circuitAlocated.getXt();
					cont++;
				}
			}
			
			averageXT =  sumXT/cont;
			if(cont==0) {
				averageXT = sumXT;
			}
			
			circuit.refreshAverageXt(i,averageXT);
		}
		
	}
	
	// Spectrum Ultilization Weighted
	private void spectrumUtilizationWeighted (Circuit circuit, ControlPlane cp) {
		Vector<Link> listaDeLinks;
		ArrayList<Integer> coresAdjacentes;
		
		listaDeLinks = circuit.getRoute().getLinkList();
		
		for (int i = 0; i<7; i++) {
			
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

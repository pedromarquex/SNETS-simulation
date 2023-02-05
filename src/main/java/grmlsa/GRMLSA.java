package grmlsa;

import grmlsa.csa.ABNEwML;
//import grmlsa.csa.IntraAreaFF;
import grmlsa.csa.AbneMlTrue;
import grmlsa.csa.CSBASDM;
import grmlsa.csa.CSBASDM2;
import grmlsa.csa.CoreAndSpectrumAssignmentAlgorithmInterface;
import grmlsa.csa.CorePrioritizationCrosstalkAvoidanceStrategy;
import grmlsa.csa.CorePrioritizationFirstFit;
import grmlsa.csa.CorePrioritizationRandomFit;
import grmlsa.csa.DiRMCSA;
import grmlsa.csa.FixedCoreFirstFit;
import grmlsa.csa.IcxtAwareAlgorithm;
import grmlsa.csa.IncrementalCoreFirstFit;
import grmlsa.csa.IncrementalCoreFirstFitv2;
import grmlsa.csa.IntraAreaFF;
import grmlsa.csa.Mlearning;
import grmlsa.csa.NewAcine;
import grmlsa.csa.NewCbaSba;
import grmlsa.csa.Pgnie;
import grmlsa.csa.RandomCoreCrosstalkAvoidanceStrategy;
import grmlsa.csa.Acine;
import grmlsa.csa.AcineMl;
import grmlsa.csa.Adein;
import grmlsa.csa.RandomCoreFirstFit;
import grmlsa.csa.RandomCoreRandomFit;
import grmlsa.csa.TestOSNR;
import grmlsa.csa.XtAwareGreedyAlgorithm;
import grmlsa.integrated.*;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistance;
import grmlsa.modulation.ModulationSelectionByDistance2;
import grmlsa.modulation.ModulationSelectionByDistanceAndBandwidth;
import grmlsa.modulation.ModulationSelectionByQoT;
import grmlsa.modulation.ModulationSelectionByQoTAndSigma;
import grmlsa.modulation.ModulationSelectionByQoTv2;
import grmlsa.modulation.ModulationTest;
import grmlsa.regeneratorAssignment.AllAssignmentOfRegenerator;
//import grmlsa.regeneratorAssignment.FLRRegeneratorAssignment;
//import grmlsa.regeneratorAssignment.FNSRegeneratorAssignment;
import grmlsa.regeneratorAssignment.RegeneratorAssignmentAlgorithmInterface;
import grmlsa.routing.DJK;
import grmlsa.routing.FixedRoutes;
import grmlsa.routing.MMRDS;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.BestFit;
//import grmlsa.spectrumAssignment.DispersionAdaptiveFirstLastFit;
import grmlsa.spectrumAssignment.ExactFit;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.FirstLastExactFit;
import grmlsa.spectrumAssignment.FirstLastFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.RandomFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
//import grmlsa.spectrumAssignment.SpectrumAssignmentWithInterferenceReduction;
//import grmlsa.spectrumAssignment.TrafficBalancingSpectrumAssignment;
import grmlsa.spectrumAssignment.WorstFit;
import grmlsa.trafficGrooming.*;

import java.io.Serializable;

/**
 * This class should be responsible for running the RSA algorithms, verifying whether the selected 
 * algorithm is of the integrated or sequential type, after activating the algorithm (s) necessary 
 * to allocate the resource to the request
 *
 * @author Iallen
 */
public class GRMLSA implements Serializable {
	
	// Network type
	public static final int TRANSPARENT = 0;
	public static final int TRANSLUCENT = 1;
	
	// Constants that indicate which type are the RSA algorithms (sequential or integrated)
    public static final int RSA_SEQUENCIAL = 0;
    public static final int RSA_INTEGRATED = 1;
	public static final int EON_SDM = 2;

    // Constants for indication of RMLSA algorithms
    // Optical traffic aggregation
    private static final String GROOMING_OPT_NOTRAFFICGROOMING = "notrafficgrooming";
    private static final String GROOMING_OPT_SIMPLETRAFFICGROOMING = "simpletrafficgrooming";
    private static final String GROOMING_OPT_MGFCCF = "mgfccf";//equivalent to mtgsr
    private static final String GROOMING_OPT_MTGSR = "mtgsr";
    private static final String GROOMING_OPT_MGFCCFSRNP = "mgfccfsrnp";//equivalent to mtgsr_srnp
    private static final String GROOMING_OPT_MTGSRSRNP = "mtgsr_srnp";

    // Routing
    private static final String ROUTING_DJK = "djk";
    private static final String ROUTING_MMRDS = "mmrds";
    private static final String ROUTING_FIXEDROUTES = "fixedroutes";

    // Spectrum assignment
    private static final String SPECTRUM_ASSIGNMENT_FISTFIT = "firstfit";
    private static final String SPECTRUM_ASSIGNMENT_BESTFIT = "bestfit";
    private static final String SPECTRUM_ASSIGNMENT_WORSTFIT = "worstfit";
    private static final String SPECTRUM_ASSIGNMENT_EXACTFIT = "exactfit";
    private static final String SPECTRUM_ASSIGNMENT_LASTFIT = "lastfit";
    private static final String SPECTRUM_ASSIGNMENT_RANDOMFIT = "randomfit";
    private static final String SPECTRUM_ASSIGNMENT_FIRSTLASTFIT = "firstlastfit";
    private static final String SPECTRUM_ASSIGNMENT_FIRSTLASTEXACTFIT = "firstlastexactfit";
    private static final String SPECTRUM_ASSIGNMENT_TBSA = "tbsa";
    private static final String SPECTRUM_ASSIGNMENT_DAFLF = "daflf";
    private static final String SPECTRUM_ASSIGNMENT_SAIR = "sair";
    
    // Integrados
    private static final String INTEGRATED_COMPLETESHARING = "completesharing";
    private static final String INTEGRATED_PSEUDOPARTITION = "pseudopartition";
    private static final String INTEGRATED_DEDICATEDPARTITION = "dedicatedpartition";
    private static final String INTEGRATED_LOADBALANCEDDEDICATEDPARTITION = "loadbalanceddedicatedpartition";
    private static final String INTEGRATED_ZONEPARTITION = "zonepartition";
    private static final String INTEGRATED_ZONEPARTITIONTOPINVASION = "zonepartitiontopinvasion";
    private static final String INTEGRATED_KSPFIRSTFIT = "kspfirstfit";
    private static final String INTEGRATED_COMPLETESHARINGEX = "completesharingex";
    private static final String INTEGRATED_COMPLETESHARINGESPAT = "completesharing_espat";
    private static final String INTEGRATED_COMPLETESHARINGEX2 = "completesharingex2";
    private static final String INTEGRATED_COMPLETESHARINGSSTG = "completesharing_sstg";
    private static final String INTEGRATED_KSPSA = "kspsa";
    private static final String INTEGRATED_KSPSA_v2 = "kspsav2";
    private static final String INTEGRATED_KSPC = "kspc";
    private static final String INTEGRATED_MDPC= "mdpc";
    private static final String INTEGRATED_KSPRQOTO = "ksprqoto";
    
    // Regenerator assignment
    private static final String ALL_ASSIGNMENT_OF_REGENERATOR = "aar";
    private static final String FLR_REGENERATOR_ASSIGNMENT = "flrra";
	private static final String FNS_REGENERATOR_ASSIGNMENT = "fnsra";
	
	// Modulation selection
	private static final String MODULATION_BY_DISTANCE = "modulationbydistance";
	private static final String MODULATION_BY_DISTANCE2 = "modulationbydistance2";
	private static final String MODULATION_BY_QOT = "modulationbyqot";
	private static final String MODULATION_BY_QOT_SIGMA = "modulationbyqotsigma";
	private static final String MODULATION_BY_QOT_V2 = "modulationbyqotv2";
	private static final String MODULATION_BY_DISTANCE_BANDWIDTH = "modulationbydistancebandwidth";
	private static final String MODULATION_TEST = "modulationtest";
	
	// Core and Spectrum assignment
	private static final String CORE_SPECTRUM_ASSIGNMENT_FIXEDCOREFIRSTFIT = "fixedcorefirstfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_INCREMENTALCOREFIRSTFIT = "incrementalcorefirstfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_INCREMENTALCOREFIRSTFITV2 = "incrementalcorefirstfitv2";
	private static final String CORE_SPECTRUM_ASSIGNMENT_RANDOMCOREFIRSTFIT = "randomcorefirstfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_RANDOMCORERANDOMFIT = "randomcorerandomfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_CSBASDM = "csbasdm"; //ABNE
	private static final String CORE_SPECTRUM_ASSIGNMENT_CSBASDM2 = "csbasdm2";
	private static final String CORE_SPECTRUM_ASSIGNMENT_COREPRIORITIZATIONFIRSTFIT = "coreprioritizationfirstfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_COREPRIORITIZATIONRANDOMFIT = "coreprioritizationrandomfit";
	private static final String CORE_SPECTRUM_ASSIGNMENT_XTAWAREGREEDYALGORITHM = "xtawaregreedyalgorithm";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ACINE = "acine";
	private static final String CORE_SPECTRUM_ASSIGNMENT_NEWACINE = "newacine";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ACINEML = "acineml";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ABNEWML = "abnewml"; //CBA-SBA
	private static final String CORE_SPECTRUM_ASSIGNMENT_MLEARNING = "mlearning";
	private static final String CORE_SPECTRUM_ASSIGNMENT_TESTOSNR = "testosnr";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ABNEMLTRUE = "abnemltrue";
	private static final String CORE_SPECTRUM_ASSIGNMENT_NEWCBASBA = "newcbasba";
	private static final String CORE_SPECTRUM_ASSIGNMENT_PGNIE = "pgnie";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ADEIN = "adein";
	private static final String CORE_SPECTRUM_ASSIGNMENT_INTRAAREAFF = "intraareaff";
	private static final String CORE_SPECTRUM_ASSIGNMENT_RCCAS = "rccas";
	private static final String CORE_SPECTRUM_ASSIGNMENT_CPCAS = "cpcas";
	private static final String CORE_SPECTRUM_ASSIGNMENT_ICXTAA = "icxtaa";
	private static final String CORE_SPECTRUM_ASSIGNMENT_DIRMCSA = "dirmcsa";
	//private static final String CORE_SPECTRUM_ASSIGNMENT_FISTFIT = "firstfitcore";
	
    // End of constants

    private String grooming;
    private String integrated;
    private String routing;
    private String modulationSelection;
    private String spectrumAssignmentType;
    private String regeneratorAssignment;
    private String coreAndSpectrumAssignmentType;

    /**
     * Creates a new instance of GRMLSA
     * 
     * @param grooming String
     * @param integrated String
     * @param routing String
     * @param modulationSelection String
     * @param spectrumAssignmentType String
     */
    public GRMLSA(String grooming, String integrated, String routing, String modulationSelection, String spectrumAssignmentType, String regeneratorAssignment, String coreAndSpectrumAssignmentType) {
        this.grooming = grooming;
        this.integrated = integrated;
        this.routing = routing;
        this.modulationSelection = modulationSelection;
        this.spectrumAssignmentType = spectrumAssignmentType;
        this.regeneratorAssignment = regeneratorAssignment;
        this.coreAndSpectrumAssignmentType = coreAndSpectrumAssignmentType;

        if(grooming == null) this.grooming ="";
        if(integrated == null) this.integrated ="";
        if(routing == null) this.routing ="";
        if(modulationSelection == null) this.modulationSelection ="";
        if(spectrumAssignmentType == null) this.spectrumAssignmentType ="";
        if(regeneratorAssignment == null) this.regeneratorAssignment = "";
        if(coreAndSpectrumAssignmentType == null) this.coreAndSpectrumAssignmentType = "";
    }

    /**
     * Instance the optical traffic aggregation algorithm
     * 
     * @throws Exception
     * @return TrafficGroomingAlgorithm
     */
    public TrafficGroomingAlgorithmInterface instantiateGrooming(){
        switch (this.grooming) {
            case GROOMING_OPT_NOTRAFFICGROOMING:
                return new NoTrafficGrooming();
//            case GROOMING_OPT_SIMPLETRAFFICGROOMING:
//                return new SimpleTrafficGrooming();
//            case GROOMING_OPT_MGFCCF:
//            case GROOMING_OPT_MTGSR: //equivalent
//                return new MTGSR();
//            case GROOMING_OPT_MGFCCFSRNP:
//            case GROOMING_OPT_MTGSRSRNP: //equivalent
//                return new MTGSR_SRNP();
            default:
                return null;
        }
    }

    /**
     * Instance the routing algorithm
     *
     * @throws Exception
     * @return RoutingInterface
     */
    public RoutingAlgorithmInterface instantiateRouting(){
        switch (this.routing) {
            case ROUTING_DJK:
                return new DJK();
            case ROUTING_FIXEDROUTES:
                return new FixedRoutes();
            case ROUTING_MMRDS:
                return new MMRDS();
            default:
                return null;
        }
    }

    /**
     * Instance the spectrum assignment algorithm
     *
     * @throws Exception
     * @return SpectrumAssignmentInterface
     */
    public SpectrumAssignmentAlgorithmInterface instantiateSpectrumAssignment(){
        switch (this.spectrumAssignmentType) {
            case SPECTRUM_ASSIGNMENT_FISTFIT:
                return new FirstFit();
            case SPECTRUM_ASSIGNMENT_BESTFIT:
                return new BestFit();
            case SPECTRUM_ASSIGNMENT_WORSTFIT:
                return new WorstFit();
            case SPECTRUM_ASSIGNMENT_EXACTFIT:
                return new ExactFit();
            case SPECTRUM_ASSIGNMENT_LASTFIT:
                return new LastFit();
            case SPECTRUM_ASSIGNMENT_RANDOMFIT:
                return new RandomFit();
            case SPECTRUM_ASSIGNMENT_FIRSTLASTFIT:
                return new FirstLastFit();
            case SPECTRUM_ASSIGNMENT_FIRSTLASTEXACTFIT:
            	return new FirstLastExactFit();
//            case SPECTRUM_ASSIGNMENT_TBSA:
//                return new TrafficBalancingSpectrumAssignment();
//            case SPECTRUM_ASSIGNMENT_DAFLF:
//                return new DispersionAdaptiveFirstLastFit();
//            case SPECTRUM_ASSIGNMENT_SAIR:
//                return new SpectrumAssignmentWithInterferenceReduction();
            default:
                return null;
        }
    }

    /**
     * Instance the integrated RMLSA algorithm
     *
     * @throws Exception
     * @return IntegratedRSAAlgoritm
     */
    public IntegratedRMLSAAlgorithmInterface instantiateIntegratedRSA(){
        switch (this.integrated) {
            case INTEGRATED_COMPLETESHARING:
                return new CompleteSharing();
//            case INTEGRATED_PSEUDOPARTITION:
//                return new PseudoPartition();
//            case INTEGRATED_DEDICATEDPARTITION:
//                return new DedicatedPartition();
//            case INTEGRATED_LOADBALANCEDDEDICATEDPARTITION:
//                return new LoadBalancedDedicatedPartition();
//            case INTEGRATED_ZONEPARTITION:
//                return new ZonePartition();
//            case INTEGRATED_ZONEPARTITIONTOPINVASION:
//                return new ZonePartitionTopInvasion();
//            case INTEGRATED_KSPFIRSTFIT:
//                return new KSPFirstFit();
//            case INTEGRATED_COMPLETESHARINGEX:
//            case INTEGRATED_COMPLETESHARINGESPAT:
//                return new CompleteSharingEsPAT();
//            case INTEGRATED_COMPLETESHARINGEX2:
//            case INTEGRATED_COMPLETESHARINGSSTG:
//                return new CompleteSharingSSTG();
//            case INTEGRATED_KSPSA:
//                return new KShortestPathsAndSpectrumAssignment();
//            case INTEGRATED_KSPSA_v2:
//                return new KShortestPathsAndSpectrumAssignment_v2();
//            case INTEGRATED_KSPC:
//                return new KShortestPathsComputation();
//            case INTEGRATED_MDPC:
//                return new ModifiedDijkstraPathsComputation();
//            case INTEGRATED_KSPRQOTO:
//            	return new KShortestPathsReductionQoTO();
            default:
                return null;
        }
    }
    
    /**
     * Instance the regenerators assignment algorithm
     * 
     * @throws Exception
     * @return RegeneratorAssignmentAlgorithmInterface
     */
    public RegeneratorAssignmentAlgorithmInterface instantiateRegeneratorAssignment(){
    	switch (this.regeneratorAssignment) {
    		case ALL_ASSIGNMENT_OF_REGENERATOR:
    			return new AllAssignmentOfRegenerator();
//    		case FLR_REGENERATOR_ASSIGNMENT:
//				return new FLRRegeneratorAssignment();
//			case FNS_REGENERATOR_ASSIGNMENT:
//				return new FNSRegeneratorAssignment();
    		default:
    			return null;
    	}
    }
    
    /**
     * Instance the modulation selection algorithm
     * 
     * @return ModulationSelectionAlgorithmInterface
     * @throws Exception
     */
    public ModulationSelectionAlgorithmInterface instantiateModulationSelection(){
    	switch (this.modulationSelection) {
	    	case MODULATION_BY_DISTANCE:
	    		return new ModulationSelectionByDistance();
	    	case MODULATION_BY_DISTANCE2:
	    		return new ModulationSelectionByDistance2();
	    	case MODULATION_BY_QOT:
	    		return new ModulationSelectionByQoT();
	    	case MODULATION_BY_QOT_SIGMA:
	    		return new ModulationSelectionByQoTAndSigma();
	    	case MODULATION_BY_QOT_V2:
	    		return new ModulationSelectionByQoTv2();
	    	case MODULATION_BY_DISTANCE_BANDWIDTH:
	    		return new ModulationSelectionByDistanceAndBandwidth();
	    	case MODULATION_TEST:
	    		return new ModulationTest();
	    	default:
	    		return null;
    	}
    }
    
    public CoreAndSpectrumAssignmentAlgorithmInterface instantiateCoreAndSpectrumAssignment(){
        switch (this.coreAndSpectrumAssignmentType) {
            case CORE_SPECTRUM_ASSIGNMENT_FIXEDCOREFIRSTFIT:
                return new FixedCoreFirstFit();
            case CORE_SPECTRUM_ASSIGNMENT_INCREMENTALCOREFIRSTFIT:
                return new IncrementalCoreFirstFit();
            case CORE_SPECTRUM_ASSIGNMENT_INCREMENTALCOREFIRSTFITV2:
                return new IncrementalCoreFirstFitv2();
            case CORE_SPECTRUM_ASSIGNMENT_RANDOMCOREFIRSTFIT:
                return new RandomCoreFirstFit();
            case CORE_SPECTRUM_ASSIGNMENT_RANDOMCORERANDOMFIT:
                return new RandomCoreRandomFit();
            case CORE_SPECTRUM_ASSIGNMENT_CSBASDM:
                return new CSBASDM();
            case CORE_SPECTRUM_ASSIGNMENT_CSBASDM2:
                return new CSBASDM2();
            case CORE_SPECTRUM_ASSIGNMENT_COREPRIORITIZATIONFIRSTFIT:
            	return new CorePrioritizationFirstFit();
            case CORE_SPECTRUM_ASSIGNMENT_COREPRIORITIZATIONRANDOMFIT:
            	return new CorePrioritizationRandomFit();
            case CORE_SPECTRUM_ASSIGNMENT_XTAWAREGREEDYALGORITHM:
            	return new XtAwareGreedyAlgorithm();
            case CORE_SPECTRUM_ASSIGNMENT_ACINE:
            	return new Acine();
            case CORE_SPECTRUM_ASSIGNMENT_NEWACINE:
            	return new NewAcine();
            case CORE_SPECTRUM_ASSIGNMENT_ACINEML:
            	return new AcineMl();
            case CORE_SPECTRUM_ASSIGNMENT_ABNEWML:
            	return new ABNEwML();
            case CORE_SPECTRUM_ASSIGNMENT_MLEARNING:
            	return new Mlearning();
            case CORE_SPECTRUM_ASSIGNMENT_TESTOSNR:
            	return new TestOSNR();
            case CORE_SPECTRUM_ASSIGNMENT_ABNEMLTRUE:
            	return new AbneMlTrue();
            case CORE_SPECTRUM_ASSIGNMENT_NEWCBASBA:
            	return new NewCbaSba();
            case CORE_SPECTRUM_ASSIGNMENT_PGNIE:
            	return new Pgnie();
            case CORE_SPECTRUM_ASSIGNMENT_ADEIN:
            	return new Adein();
            case CORE_SPECTRUM_ASSIGNMENT_INTRAAREAFF:
            	return new IntraAreaFF();
            case CORE_SPECTRUM_ASSIGNMENT_RCCAS:
            	return new RandomCoreCrosstalkAvoidanceStrategy();
            case CORE_SPECTRUM_ASSIGNMENT_CPCAS:
            	return new CorePrioritizationCrosstalkAvoidanceStrategy();
            case CORE_SPECTRUM_ASSIGNMENT_ICXTAA:
            	return new IcxtAwareAlgorithm();
            case CORE_SPECTRUM_ASSIGNMENT_DIRMCSA:
            	return new DiRMCSA();
            default:
                return null;
        }
    }
}

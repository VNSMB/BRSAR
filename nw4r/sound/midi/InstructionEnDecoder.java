package nw4r.sound.midi;

import static java.util.Map.entry;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class InstructionEnDecoder {

    private static final Map<Byte, String> MML_INSTRUCTION_DEC;
    private static final Map<Byte, String> MMLEX_INSTRUCTION_DEC;
    
    private static final Map<String, Byte> MML_INSTRUCTION_ENC;
    private static final Map<String, Byte> MMLEX_INSTRUCTION_ENC;
    
    static {
        Map<Byte, String> map = Map.ofEntries(
            entry(RSEQ.MML_WAIT, "wait"),
            entry(RSEQ.MML_PRG, "prg"),
            
            entry(RSEQ.MML_OPEN_TRACK, "opentrack"),
            entry(RSEQ.MML_JUMP, "jump"),
            entry(RSEQ.MML_CALL, "call"),
            
            entry(RSEQ.MML_RANDOM, "_r"),
            entry(RSEQ.MML_VARIABLE, "_v"),
            entry(RSEQ.MML_IF, "_if"),
            entry(RSEQ.MML_TIME, "_t"),
            entry(RSEQ.MML_TIME_RANDOM, "_tr"),
            entry(RSEQ.MML_TIME_VARIABLE, "_tv"),
            entry(RSEQ.MML_TIMEBASE, "timebase"),
            entry(RSEQ.MML_ENV_HOLD, "env_hold"),
            entry(RSEQ.MML_MONOPHONIC, "monophonic_"),
            entry(RSEQ.MML_VELOCITY_RANGE, "velocity_range"),
            entry(RSEQ.MML_BIQUAD_TYPE, "biquad_type"),
            entry(RSEQ.MML_BIQUAD_VALUE, "biquad_value"),
            entry(RSEQ.MML_PAN, "pan"),
            entry(RSEQ.MML_VOLUME, "volume"),
            entry(RSEQ.MML_MAIN_VOLUME, "main_volume"),
            entry(RSEQ.MML_TRANSPOSE, "transpose"),
            entry(RSEQ.MML_PITCH_BEND, "pitch_bend"),
            entry(RSEQ.MML_BEND_RANGE, "bend_range"),
            entry(RSEQ.MML_PRIO, "prio"),
            entry(RSEQ.MML_NOTE_WAIT, "notewait_"),
            entry(RSEQ.MML_TIE, "tie"),
            entry(RSEQ.MML_PORTA, "porta"),
            entry(RSEQ.MML_MOD_DEPTH, "mod_depth"),
            entry(RSEQ.MML_MOD_SPEED, "mod_speed"),
            entry(RSEQ.MML_MOD_TYPE, "mod_type"),
            entry(RSEQ.MML_MOD_RANGE, "mod_range"),
            entry(RSEQ.MML_PORTA_SW, "porta_"),
            entry(RSEQ.MML_PORTA_TIME, "porta_time"),
            entry(RSEQ.MML_ATTACK, "attack"),
            entry(RSEQ.MML_DECAY, "decay"),
            entry(RSEQ.MML_SUSTAIN, "sustain"),
            entry(RSEQ.MML_RELEASE, "release"),
            entry(RSEQ.MML_LOOP_START, "loop_start"),
            entry(RSEQ.MML_VOLUME2, "volume2"),
            entry(RSEQ.MML_PRINTVAR, "printvar"),
            entry(RSEQ.MML_SURROUND_PAN, "span"),
            entry(RSEQ.MML_LPF_CUTOFF, "lpf_cutoff"),
            entry(RSEQ.MML_FXSEND_A, "fxsend_a"),
            entry(RSEQ.MML_FXSEND_B, "fxsend_b"),
            entry(RSEQ.MML_MAINSEND, "mainsend"),
            entry(RSEQ.MML_INIT_PAN, "init_pan"),
            entry(RSEQ.MML_MUTE, "mute"),
            entry(RSEQ.MML_FXSEND_C, "fxsend_c"),
            entry(RSEQ.MML_DAMPER, "damper_"),
            
            entry(RSEQ.MML_MOD_DELAY, "mod_delay"),
            entry(RSEQ.MML_TEMPO, "tempo"),
            entry(RSEQ.MML_SWEEP_PITCH, "sweep_pitch"),
            entry(RSEQ.MML_EX_COMMAND, ""), //special
            
            entry(RSEQ.MML_ENV_RESET, "env_reset"),
            entry(RSEQ.MML_LOOP_END, "loop_end"),
            entry(RSEQ.MML_RET, "ret"),
            entry(RSEQ.MML_ALLOC_TRACK, "alloctrack"),
            entry(RSEQ.MML_FIN, "fin")
        );
        
        Map<Byte, String> ex = Map.ofEntries(
            entry(RSEQ.MMLEX_SETVAR, "setvar"),
            entry(RSEQ.MMLEX_ADDVAR, "addvar"),
            entry(RSEQ.MMLEX_SUBVAR, "subvar"),
            entry(RSEQ.MMLEX_MULVAR, "mulvar"),
            entry(RSEQ.MMLEX_DIVVAR, "divvar"),
            entry(RSEQ.MMLEX_SHIFTVAR, "shiftvar"),
            entry(RSEQ.MMLEX_RANDVAR, "randvar"),
            entry(RSEQ.MMLEX_ANDVAR, "andvar"),
            entry(RSEQ.MMLEX_ORVAR, "orvar"),
            entry(RSEQ.MMLEX_XORVAR, "xorvar"),
            entry(RSEQ.MMLEX_NOTVAR, "notvar"),
            entry(RSEQ.MMLEX_MODVAR, "modvar"),
            
            entry(RSEQ.MMLX_CMP_EQ, "cmp_eq"),
            entry(RSEQ.MMLX_CMP_GE, "cmp_ge"),
            entry(RSEQ.MMLX_CMP_GT, "cmp_gt"),
            entry(RSEQ.MMLX_CMP_LE, "cmp_le"),
            entry(RSEQ.MMLX_CMP_LT, "cmp_lt"),
            entry(RSEQ.MMLX_CMP_NE, "cmp_ne"),
            
            entry(RSEQ.MMLX_USERPROC, "userproc")
        );
       
        MML_INSTRUCTION_DEC = Collections.unmodifiableMap(map);
        MMLEX_INSTRUCTION_DEC = Collections.unmodifiableMap(ex);
        MML_INSTRUCTION_ENC = Collections.unmodifiableMap(
            map.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
        MMLEX_INSTRUCTION_ENC = Collections.unmodifiableMap(
            ex.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
    }
    
    private InstructionEnDecoder() {}
    
    public static String decodeMMLInstruction(byte opcode) {
        return MML_INSTRUCTION_DEC.get(opcode);
    }

    public static String decodeMMLEXInstruction(byte opcode) {
        return MMLEX_INSTRUCTION_DEC.get(opcode);
    }
}
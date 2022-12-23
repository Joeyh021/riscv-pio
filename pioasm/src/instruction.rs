pub enum Instruction {
    Jmp {
        delay_side_set: u16,
        condition: u16,
        address: u16,
    },
    Wait {
        delay_side_set: u16,
        polarity: u16,
        source: u16,
        index: u16,
    },
    In {
        delay_side_set: u16,
        source: u16,
        bit_count: u16,
    },
    Out {
        delay_side_set: u16,
        dest: u16,
        bit_count: u16,
    },
    Push {
        delay_side_set: u16,
        iff: u16,
        blk: u16,
    },
    Pull {
        delay_side_set: u16,
        ife: u16,
        blk: u16,
    },
    Mov {
        delay_side_set: u16,
        dest: u16,
        op: u16,
        src: u16,
    },
    Set {
        delay_side_set: u16,
        dest: u16,
        data: u16,
    },
}

impl Instruction {
    pub fn assemble(&self) -> u16 {
        match *self {
            Instruction::Jmp {
                delay_side_set, //always 5 bits
                condition,      //3 bits
                address,        //5 bits
            } => delay_side_set << 8 | condition << 5 | address,
            Instruction::Wait {
                delay_side_set,
                polarity, //1 bit
                source,   //2 bits
                index,    //5 bits
            } => 1 | delay_side_set << 8 | polarity << 7 | source << 5 | index,
            Instruction::In {
                delay_side_set,
                source,    //3 bits
                bit_count, //5 bits
            } => 2 | delay_side_set << 8 | source << 5 | bit_count,
            Instruction::Out {
                delay_side_set,
                dest,      //3 bits
                bit_count, // 5 bits
            } => 3 | delay_side_set << 8 | dest << 5 | bit_count,
            Instruction::Push {
                delay_side_set,
                iff, //1 bit
                blk, //1 bit
            } => 4 | delay_side_set << 8 | iff << 6 | blk << 5,
            Instruction::Pull {
                delay_side_set,
                ife, //1 bit
                blk, //1 bit
            } => 4 | delay_side_set << 8 | 1 << 7 | ife << 6 | blk << 5,
            Instruction::Mov {
                delay_side_set,
                dest, //3 bits
                op,   //2 bits
                src,  //3 bits
            } => 5 | delay_side_set << 8 | dest << 5 | op << 3 | src,
            Instruction::Set {
                delay_side_set,
                dest, //3 bits
                data, //5 bits
            } => 7 | delay_side_set << 8 | dest << 5 | data,
        }
    }
}

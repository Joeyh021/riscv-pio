gen:
    sbt "runMain dev.joeyh.Main"    
    rm gen/*.fir gen/*.anno.json

test:
    sbt test

clean:
    rm -rf gen/*

CROSS_COMPILER = /home/joey/vivado-risc-v/workspace/gcc/riscv/bin/riscv64-unknown-elf-

CC=$(CROSS_COMPILE)gcc
OBJCOPY=$(CROSS_COMPILE)objcopy
OBJDUMP=$(CROSS_COMPILE)objdump
CCFLAGS=-march=rv64gc -mabi=lp64d -fno-builtin -ffreestanding -mcmodel=medany -O0 -Wall -fno-pic -fno-common -g -I.

LFLAGS=-static -nostartfiles -T main.lds

binary: startup.S src/*.rs
	cargo build --release
	$(CC) $(CCFLAGS) $(LFLAGS) -o $@ startup.S target/riscv64gc-unknown-none-elf/release/librust.a
	$(OBJDUMP) -h -p $@

flash:
	cp boot.elf /media/joey/BOOT/boot.elf


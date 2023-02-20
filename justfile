gen:
    sbt "runMain dev.joeyh.Main"    
    rm gen/*.fir gen/*.anno.json

test:
    sbt test

clean:
    rm -rf gen/*

CROSS_COMPILE:= "/home/joey/vivado-risc-v/workspace/gcc/riscv/bin/riscv64-unknown-elf-"

CC:=CROSS_COMPILE + "gcc"
OBJCOPY:=CROSS_COMPILE + "objcopy"
OBJDUMP:=CROSS_COMPILE + "objdump"
CCFLAGS:="-march=rv64gc -mabi=lp64d -fno-builtin -ffreestanding -mcmodel=medany -O0 -Wall -fno-pic -fno-common -g -I."

LFLAGS:="-static -nostartfiles -T drivers/main.lds"

binary: 
	cargo build --manifest-path drivers/Cargo.toml --release
	{{CC}} {{CCFLAGS}} {{LFLAGS}} -o $@ driver/startup.S drivers/target/riscv64gc-unknown-none-elf/release/librust.a
	{{OBJDUMP}} -h -p $@

flash:
	cp boot.elf /media/joey/BOOT/boot.elf


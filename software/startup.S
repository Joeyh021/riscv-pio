
.section .text.start, "ax", @progbits
.globl _start
_start:
  li sp, 0x81000000
  call main
_hang:
  wfi
  j _hang

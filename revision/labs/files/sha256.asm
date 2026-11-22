# Independent SHA-256 exercise: implement the six entries.
# Preserve supplied caller objects; constants are given.
.data
.align 6
data: .byte 97,98,99,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24
      .byte -91:4096
data_end: .word 0x13579bdf
.align 6
state: .word 0x6a6b6c6d:24
state_end:
schedule: .word 0x6a6b6c6d:80
schedule_end:
context: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    .dword 0
    .word 0,0
    .byte -91:784
context_end:
digest: .byte -91:96
digest_end:
path: .asciiz "fixture.bin"
      .space 116
path_end:
scratch: .byte -91:320
scratch_end:
length: .word 3
mode: .word 2
buffer_capacity: .word 7
status: .word -1
.align 3
sha_iv: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
sha_k: .word 0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, mode
    lw $t0, 0($t0)
    beqzc $t0, main_memory
    nop
    li $t1, 1
    beqc $t0, $t1, main_file
    nop
    bc main_exercise
    nop
main_memory:
    dla $a0, context
    balc sha256_init
    nop
    dla $a0, context
    dla $a1, data
    dla $t0, length
    lwu $a2, 0($t0)
memory_call:
    balc sha256_update
    nop
    dla $a0, context
    dla $a1, digest
    balc sha256_final
    nop
    bc main_done
    nop
main_file:
    dla $a0, path
    dla $a1, scratch
    dla $t0, buffer_capacity
    lwu $a2, 0($t0)
    dla $a3, digest
file_call:
    balc sha256_file
    nop
    bc main_done
    nop
main_exercise:
    li $t1, 2
    beqc $t0, $t1, main_schedule
    nop
    li $t1, 3
    beqc $t0, $t1, main_compress
    nop
    li $t1, 4
    beqc $t0, $t1, main_init
    nop
    li $t1, 5
    beqc $t0, $t1, main_update
    nop
    dla $a0, context
    dla $a1, digest
    dla $t9, sha256_final
    bc kernel_call
    nop
main_schedule:
    dla $a0, data
    dla $a1, schedule
    dla $t9, sha256_schedule
    bc kernel_call
    nop
main_compress:
    dla $a0, state
    dla $a1, data
    dla $a2, schedule
    dla $t9, sha256_compress
    bc kernel_call
    nop
main_init:
    dla $a0, context
    dla $t9, sha256_init
    bc kernel_call
    nop
main_update:
    dla $a0, context
    dla $a1, data
    dla $t0, length
    lwu $a2, 0($t0)
    dla $t9, sha256_update
kernel_call:
    jalr $t9
    nop
    bc main_done
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    bc results_ready
    nop


sha256_schedule:
    break

sha256_compress:
    break

sha256_init:
    break

sha256_update:
    break

sha256_final:
    break

sha256_file:
    break

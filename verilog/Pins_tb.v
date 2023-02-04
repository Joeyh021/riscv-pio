module Pins_tb();

    reg [8:0] inBase, inCount, outBase, outCount;
    reg [31:0] write_data;
    reg write_enable;
    wire [31:0] read;
    wire [31:0] pins;

    Pins uut (.cfg_inBase(inBase),
        .cfg_inCount(inCount),
        .cfg_outBase(outBase), 
        .cfg_outCount(outCount), 
        .write_data(write_data), 
        .write_enable(write_enable), 
        .read(read),
        .pins(pins)
    );

    initial begin
        inBase = 8'd0;
        inCount = 8'd1;
        outBase = 8'd2;
        outCount = 8'd1;
        write_data = 32'b101;
        write_enable = 1'b1;
    end

endmodule
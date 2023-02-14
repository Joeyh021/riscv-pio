//simple clock divider since firrtl wOnt EmiT cLoCk mUx
module ClockDivider(
    input clock,
    input [16:0] divisor,
    output reg outClk
);

    reg [16:0] counter;

    always @ (posedge clock) begin
        if (counter == divisor - 16'b1) begin
            counter <= 16'b0;
            outClk <= 1'b1;
        end else begin
            counter <= counter + 16'b1;
            outClk <= 1'b0;
        end
    end


endmodule
//simple clock divider since firrtl wOnt EmiT cLoCk mUx
module ClockDivider(
    input inclk, reset,
    input [16:0] divisor,
    output outclk,
);

    reg [16:0] counter;

    always @ (posedge inclk) begin
        if(reset) begin
            counter <= 16'b0;
            outclk <= 1'b0;
        end else if (counter == divisor - 16'b1) begin
            counter <= 16'b0;
            outclk <= 1'b1;
        end else begin
            counter <= counter + 16'b1;
            outclk <= 1'b0;
        end
    end


endmodule
using System;
using Game1;

namespace NroClientTests
{
    public static class ClientProtocolTest
    {
        private static void AssertTrue(bool cond, string msg)
        {
            if (!cond)
            {
                throw new Exception("ASSERTION FAILED: " + msg);
            }
        }

        public static int Main(string[] args)
        {
            Console.WriteLine("============================================================");
            Console.WriteLine("   CLIENT PROTOCOL & DISPATCHER CONTRACT TESTS (C#)");
            Console.WriteLine("============================================================");

            TestNetworkOpcodesSemantics();
            TestMyWriterSByteAndBuffer();
            TestMyReaderReadExactBytes();

            Console.WriteLine("[ALL PASS] Client protocol contract tests passed!");
            return 0;
        }

        private static void TestNetworkOpcodesSemantics()
        {
            AssertTrue(NetworkOpcodes.SERVER_ALERT == -26, "SERVER_ALERT must be -26");
            AssertTrue(NetworkOpcodes.UPDATE_CURRENCY == 6, "UPDATE_CURRENCY must be 6");
            AssertTrue(NetworkOpcodes.UPDATE_MAX_STAMINA == -69, "UPDATE_MAX_STAMINA must be -69");
            AssertTrue(NetworkOpcodes.UPDATE_STAMINA == -68, "UPDATE_STAMINA must be -68");
            AssertTrue(NetworkOpcodes.NPC_BIG_MESSAGE == -70, "NPC_BIG_MESSAGE must be -70");
            AssertTrue(NetworkOpcodes.OPEN_UI_SAY == 38, "OPEN_UI_SAY must be 38");
            AssertTrue(NetworkOpcodes.NPC_MENU == 32, "NPC_MENU must be 32");
            Console.WriteLine("[PASS] NetworkOpcodes semantic constants");
        }

        private static void TestMyWriterSByteAndBuffer()
        {
            myWriter writer = new myWriter();
            for (int i = 0; i < 3000; i++)
            {
                writer.writeSByte((sbyte)(i % 127));
            }
            sbyte[] data = writer.getData();
            AssertTrue(data.Length == 3000, "myWriter must expand buffer cleanly without IndexOutOfRange");
            AssertTrue(data[2999] == (sbyte)(2999 % 127), "Last byte must match written value");
            Console.WriteLine("[PASS] myWriter.writeSByte auto-expand & off-by-one guard");
        }

        private static void TestMyReaderReadExactBytes()
        {
            myWriter writer = new myWriter();
            writer.writeInt(987654321);
            writer.writeUTF("NRO_TEST");
            sbyte[] raw = writer.getData();

            myReader reader = new myReader(raw);
            AssertTrue(reader.available() == raw.Length, "Reader available must match raw length");
            AssertTrue(reader.readInt() == 987654321, "readInt round-trip must match");
            AssertTrue(reader.readUTF() == "NRO_TEST", "readUTF round-trip must match");
            AssertTrue(reader.available() == 0, "Reader must be at EOF");
            Console.WriteLine("[PASS] myReader Big-Endian int & UTF round-trip");
        }
    }
}

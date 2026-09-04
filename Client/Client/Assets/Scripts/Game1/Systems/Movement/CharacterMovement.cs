namespace Game1.Systems.Movement
{
    using System;
    using Game1;
    using Char = Game1.Char;

    /// <summary>
    /// CharacterMovement: Module chuyên trách xử lý vật lý va chạm địa hình, di chuyển và trọng lực của nhân vật.
    /// Giúp phân tách logic vật lý phức tạp ra khỏi class khổng lồ Char.cs.
    /// </summary>
    public class CharacterMovement
    {
        private readonly Char owner;

        public int X { get; set; }
        public int Y { get; set; }
        public int Direction { get; set; } = 1; // 1: phải, -1: trái
        public int Status { get; set; } = 1;

        public bool IsFlying { get; set; }
        public bool IsGrounded { get; private set; }
        public int DelayFall { get; set; }

        public CharacterMovement(Char owner)
        {
            this.owner = owner ?? throw new ArgumentNullException(nameof(owner));
        }

        public void SyncPosition(int x, int y, int dir)
        {
            X = x;
            Y = y;
            Direction = dir;
        }

        /// <summary>
        /// Kiểm tra va chạm mặt đất với TileMap tại tọa độ chân nhân vật.
        /// </summary>
        public bool CheckGroundCollision(int px, int py)
        {
            bool grounded = TileMap.tileTypeAt(px, py, 2);
            IsGrounded = grounded;
            return grounded;
        }

        /// <summary>
        /// Tính toán khoảng cách di chuyển an toàn không vượt quá tốc độ tối đa cho phép.
        /// </summary>
        public int CalculateMoveDelta(int targetX, int speed)
        {
            int delta = targetX - X;
            if (System.Math.Abs(delta) > speed)
            {
                return delta > 0 ? speed : -speed;
            }
            return delta;
        }

        /// <summary>
        /// Cập nhật hướng quay mặt theo mục tiêu.
        /// </summary>
        public void FaceTarget(int targetX)
        {
            if (targetX > X)
            {
                Direction = 1;
            }
            else if (targetX < X)
            {
                Direction = -1;
            }
        }
    }
}

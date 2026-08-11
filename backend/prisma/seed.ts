import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  const passwordHash = await bcrypt.hash('password123', 12);

  const jane = await prisma.user.upsert({
    where: { email: 'jane.doe@example.com' },
    update: {},
    create: {
      email: 'jane.doe@example.com',
      passwordHash,
      firstName: 'Jane',
      lastName: 'Doe',
      headline: 'Software Engineer at Acme',
      company: 'Acme Corp',
      bio: 'Building things and rebuilding old university projects.',
    },
  });

  const john = await prisma.user.upsert({
    where: { email: 'john.smith@example.com' },
    update: {},
    create: {
      email: 'john.smith@example.com',
      passwordHash,
      firstName: 'John',
      lastName: 'Smith',
      headline: 'Product Manager',
      company: 'Globex',
    },
  });

  await prisma.post.create({
    data: {
      authorId: jane.id,
      content: 'This is my first post.',
      comments: {
        create: [{ authorId: john.id, content: 'Yay!' }],
      },
      likes: {
        create: [{ userId: john.id }],
      },
    },
  });

  console.log('Seed complete. Test login: jane.doe@example.com / password123');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });

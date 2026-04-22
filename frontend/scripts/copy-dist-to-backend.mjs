import { cp, mkdir, rm } from 'node:fs/promises';
import { resolve } from 'node:path';

const sourceDir = resolve(process.cwd(), 'dist');
const targetDir = resolve(process.cwd(), '..', 'src', 'main', 'resources', 'static', 'app');

await rm(targetDir, { recursive: true, force: true });
await mkdir(targetDir, { recursive: true });
await cp(sourceDir, targetDir, { recursive: true });

console.log(`Copied frontend build from ${sourceDir} to ${targetDir}`);
